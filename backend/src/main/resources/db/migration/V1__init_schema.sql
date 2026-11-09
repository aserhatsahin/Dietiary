-- Initial schema. Generated from the JPA entities, then cleaned up:
-- named constraints, indexes on foreign keys, and a few integrity fixes noted inline.

-- ============ Users and auth ============

CREATE TABLE users (
    id            UUID PRIMARY KEY,
    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(255) NOT NULL,
    role          VARCHAR(255) NOT NULL,
    created_at    TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at    TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT ck_users_role CHECK (role IN ('DIETITIAN', 'CLIENT'))
);

CREATE TABLE refresh_tokens (
    id         UUID PRIMARY KEY,
    user_id    UUID NOT NULL,
    token      VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_refresh_tokens_token UNIQUE (token),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);
CREATE INDEX ix_refresh_tokens_user ON refresh_tokens (user_id);

-- ============ Clients ============

CREATE TABLE clients (
    id           UUID PRIMARY KEY,
    dietitian_id UUID NOT NULL,
    user_id      UUID,
    full_name    VARCHAR(255) NOT NULL,
    email        VARCHAR(255) NOT NULL,
    phone        VARCHAR(255),
    birth_date   DATE,
    height_cm    NUMERIC(5, 2),
    gender       VARCHAR(255),
    health_info  TEXT,
    active       BOOLEAN NOT NULL,
    created_at   TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at   TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    deleted_at   TIMESTAMP(6) WITH TIME ZONE,
    CONSTRAINT uk_clients_dietitian_email UNIQUE (dietitian_id, email),
    CONSTRAINT ck_clients_gender CHECK (gender IN ('MALE', 'FEMALE', 'OTHER')),
    CONSTRAINT fk_clients_dietitian FOREIGN KEY (dietitian_id) REFERENCES users (id),
    CONSTRAINT fk_clients_user FOREIGN KEY (user_id) REFERENCES users (id)
);
CREATE INDEX ix_clients_user ON clients (user_id);

-- The entity stores client_id as a plain UUID; the FK and the one-invite-per-client
-- rule are enforced here (ClientInviteService reuses the existing row).
CREATE TABLE client_invites (
    id          UUID PRIMARY KEY,
    client_id   UUID NOT NULL,
    email       VARCHAR(255) NOT NULL,
    token       VARCHAR(255) NOT NULL,
    expires_at  TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    accepted_at TIMESTAMP(6) WITH TIME ZONE,
    created_at  TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_client_invites_token UNIQUE (token),
    CONSTRAINT uk_client_invites_client UNIQUE (client_id),
    CONSTRAINT fk_client_invites_client FOREIGN KEY (client_id) REFERENCES clients (id)
);

-- ============ Measurements and goals ============

CREATE TABLE measurements (
    id                   UUID PRIMARY KEY,
    client_id            UUID NOT NULL,
    measurement_date     DATE NOT NULL,
    weight_kg            NUMERIC(5, 2),
    bmi                  NUMERIC(5, 2),
    body_fat_percentage  NUMERIC(5, 2),
    muscle_mass_kg       NUMERIC(5, 2),
    water_percentage     NUMERIC(5, 2),
    bone_mass_kg         NUMERIC(5, 2),
    visceral_fat         INTEGER,
    basal_metabolic_rate INTEGER,
    metabolic_age        INTEGER,
    entry_method         VARCHAR(255),
    notes                TEXT,
    created_at           TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT ck_measurements_entry_method CHECK (entry_method IN ('MANUAL', 'PDF')),
    CONSTRAINT fk_measurements_client FOREIGN KEY (client_id) REFERENCES clients (id)
);
CREATE INDEX ix_measurements_client_date ON measurements (client_id, measurement_date);

CREATE TABLE goals (
    id                      UUID PRIMARY KEY,
    client_id               UUID NOT NULL,
    goal_type               VARCHAR(255) NOT NULL,
    activity_level          VARCHAR(255) NOT NULL,
    target_weight_kg        NUMERIC(5, 2),
    weekly_weight_change_kg NUMERIC(4, 2),
    bmr                     INTEGER NOT NULL,
    tdee                    INTEGER NOT NULL,
    daily_calorie_target    INTEGER NOT NULL,
    protein_grams           INTEGER,
    carbs_grams             INTEGER,
    fat_grams               INTEGER,
    is_active               BOOLEAN NOT NULL,
    notes                   TEXT,
    created_at              TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at              TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT ck_goals_goal_type CHECK (goal_type IN ('LOSE_WEIGHT', 'MAINTAIN_WEIGHT', 'GAIN_WEIGHT')),
    CONSTRAINT ck_goals_activity_level CHECK (activity_level IN ('SEDENTARY', 'LIGHT', 'MODERATE', 'ACTIVE', 'VERY_ACTIVE')),
    CONSTRAINT fk_goals_client FOREIGN KEY (client_id) REFERENCES clients (id)
);
CREATE INDEX ix_goals_client ON goals (client_id);

-- ============ Foods ============

-- dietitian_id is NULL for SYSTEM foods and set for a dietitian's CUSTOM foods.
CREATE TABLE foods (
    id                   UUID PRIMARY KEY,
    dietitian_id         UUID,
    name                 VARCHAR(255) NOT NULL,
    brand                VARCHAR(255),
    category             VARCHAR(255),
    serving_size         VARCHAR(255) NOT NULL,
    serving_unit         VARCHAR(255) NOT NULL,
    calories_per_serving INTEGER NOT NULL,
    protein_grams        NUMERIC(6, 2),
    carbs_grams          NUMERIC(6, 2),
    fat_grams            NUMERIC(6, 2),
    fiber_grams          NUMERIC(6, 2),
    sugar_grams          NUMERIC(6, 2),
    sodium_mg            NUMERIC(8, 2),
    source               VARCHAR(255) NOT NULL,
    active               BOOLEAN NOT NULL,
    notes                TEXT,
    created_at           TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at           TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT ck_foods_source CHECK (source IN ('SYSTEM', 'CUSTOM')),
    CONSTRAINT fk_foods_dietitian FOREIGN KEY (dietitian_id) REFERENCES users (id)
);
CREATE INDEX ix_foods_dietitian ON foods (dietitian_id);

-- ============ Meal plans ============

-- client_id is NULL for templates (the entity previously required it, so templates could not be saved).
CREATE TABLE meal_plans (
    id             UUID PRIMARY KEY,
    dietitian_id   UUID NOT NULL,
    client_id      UUID,
    name           VARCHAR(255) NOT NULL,
    description    TEXT,
    daily_calories INTEGER,
    is_active      BOOLEAN NOT NULL,
    created_at     TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at     TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_meal_plans_dietitian FOREIGN KEY (dietitian_id) REFERENCES users (id),
    CONSTRAINT fk_meal_plans_client FOREIGN KEY (client_id) REFERENCES clients (id)
);
CREATE INDEX ix_meal_plans_dietitian ON meal_plans (dietitian_id);
CREATE INDEX ix_meal_plans_client ON meal_plans (client_id);

CREATE TABLE meals (
    id            UUID PRIMARY KEY,
    meal_plan_id  UUID NOT NULL,
    meal_type     VARCHAR(255) NOT NULL,
    name          VARCHAR(255) NOT NULL,
    display_order INTEGER,
    created_at    TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT ck_meals_meal_type CHECK (meal_type IN
        ('BREAKFAST', 'MORNING_SNACK', 'LUNCH', 'AFTERNOON_SNACK', 'DINNER', 'EVENING_SNACK')),
    CONSTRAINT fk_meals_meal_plan FOREIGN KEY (meal_plan_id) REFERENCES meal_plans (id)
);
CREATE INDEX ix_meals_meal_plan ON meals (meal_plan_id);

CREATE TABLE meal_options (
    id             UUID PRIMARY KEY,
    meal_id        UUID NOT NULL,
    name           VARCHAR(255) NOT NULL,
    description    TEXT,
    total_calories INTEGER,
    display_order  INTEGER,
    created_at     TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_meal_options_meal FOREIGN KEY (meal_id) REFERENCES meals (id)
);
CREATE INDEX ix_meal_options_meal ON meal_options (meal_id);

CREATE TABLE meal_option_items (
    id             UUID PRIMARY KEY,
    meal_option_id UUID NOT NULL,
    food_id        UUID NOT NULL,
    quantity       NUMERIC(8, 2) NOT NULL,
    quantity_unit  VARCHAR(255) NOT NULL,
    calories       INTEGER,
    display_order  INTEGER,
    notes          TEXT,
    created_at     TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_meal_option_items_option FOREIGN KEY (meal_option_id) REFERENCES meal_options (id),
    CONSTRAINT fk_meal_option_items_food FOREIGN KEY (food_id) REFERENCES foods (id)
);
CREATE INDEX ix_meal_option_items_option ON meal_option_items (meal_option_id);
CREATE INDEX ix_meal_option_items_food ON meal_option_items (food_id);

-- ============ Tracking ============

CREATE TABLE daily_tracking (
    id                 UUID PRIMARY KEY,
    client_id          UUID NOT NULL,
    meal_id            UUID NOT NULL,
    selected_option_id UUID NOT NULL,
    tracking_date      DATE NOT NULL,
    is_completed       BOOLEAN NOT NULL,
    completed_at       TIMESTAMP(6) WITH TIME ZONE,
    notes              TEXT,
    created_at         TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at         TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_daily_tracking_client_date_meal UNIQUE (client_id, tracking_date, meal_id),
    CONSTRAINT fk_daily_tracking_client FOREIGN KEY (client_id) REFERENCES clients (id),
    CONSTRAINT fk_daily_tracking_meal FOREIGN KEY (meal_id) REFERENCES meals (id),
    CONSTRAINT fk_daily_tracking_option FOREIGN KEY (selected_option_id) REFERENCES meal_options (id)
);

CREATE TABLE water_tracking (
    id            UUID PRIMARY KEY,
    client_id     UUID NOT NULL,
    amount_ml     INTEGER NOT NULL,
    tracking_date DATE NOT NULL,
    logged_at     TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    created_at    TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_water_tracking_client FOREIGN KEY (client_id) REFERENCES clients (id)
);
CREATE INDEX ix_water_tracking_client_date ON water_tracking (client_id, tracking_date);
