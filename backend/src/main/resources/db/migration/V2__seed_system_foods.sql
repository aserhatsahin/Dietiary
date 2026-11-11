-- System foods available to every dietitian (source = SYSTEM, no owner).
-- Previously a dev-only startup runner seeded these, so production databases had none.
-- Skipped if system foods already exist (databases seeded by that runner).

INSERT INTO foods (id, dietitian_id, name, category, serving_size, serving_unit, calories_per_serving,
                   protein_grams, carbs_grams, fat_grams, source, active, created_at, updated_at)
SELECT gen_random_uuid(), NULL, v.name, v.category, v.serving_size, v.serving_unit, v.calories,
       v.protein, v.carbs, v.fat, 'SYSTEM', TRUE, now(), now()
FROM (VALUES
    ('Chicken Breast (Grilled)', 'Protein', '100', 'g', 165, 31, 0, 3.6),
    ('Salmon (Baked)', 'Protein', '100', 'g', 208, 20, 0, 13),
    ('Eggs (Whole, Boiled)', 'Protein', '1', 'large', 78, 6.3, 0.6, 5.3),
    ('Greek Yogurt (Plain, Non-fat)', 'Dairy', '170', 'g', 100, 17, 6, 0.7),
    ('Cottage Cheese (Low-fat)', 'Dairy', '100', 'g', 72, 12, 2.7, 1),
    ('Brown Rice (Cooked)', 'Grains', '100', 'g', 112, 2.6, 24, 0.9),
    ('Oatmeal (Cooked)', 'Grains', '100', 'g', 71, 2.5, 12, 1.5),
    ('Sweet Potato (Baked)', 'Vegetables', '100', 'g', 90, 2, 21, 0.1),
    ('Quinoa (Cooked)', 'Grains', '100', 'g', 120, 4.4, 21, 1.9),
    ('Whole Wheat Bread', 'Grains', '1', 'slice', 81, 4, 14, 1.1),
    ('Broccoli (Steamed)', 'Vegetables', '100', 'g', 35, 2.4, 7, 0.4),
    ('Spinach (Raw)', 'Vegetables', '100', 'g', 23, 2.9, 3.6, 0.4),
    ('Avocado', 'Vegetables', '100', 'g', 160, 2, 9, 15),
    ('Tomatoes (Raw)', 'Vegetables', '100', 'g', 18, 0.9, 3.9, 0.2),
    ('Carrots (Raw)', 'Vegetables', '100', 'g', 41, 0.9, 10, 0.2),
    ('Banana', 'Fruits', '1', 'medium', 105, 1.3, 27, 0.4),
    ('Apple', 'Fruits', '1', 'medium', 95, 0.5, 25, 0.3),
    ('Blueberries', 'Fruits', '100', 'g', 57, 0.7, 14, 0.3),
    ('Orange', 'Fruits', '1', 'medium', 62, 1.2, 15, 0.2),
    ('Strawberries', 'Fruits', '100', 'g', 32, 0.7, 8, 0.3),
    ('Almonds', 'Nuts & Seeds', '28', 'g', 164, 6, 6, 14),
    ('Walnuts', 'Nuts & Seeds', '28', 'g', 185, 4.3, 4, 18),
    ('Chia Seeds', 'Nuts & Seeds', '28', 'g', 137, 4.4, 12, 9),
    ('Olive Oil', 'Oils & Fats', '1', 'tbsp', 119, 0, 0, 14),
    ('Coconut Oil', 'Oils & Fats', '1', 'tbsp', 121, 0, 0, 13)
) AS v (name, category, serving_size, serving_unit, calories, protein, carbs, fat)
WHERE NOT EXISTS (SELECT 1 FROM foods WHERE source = 'SYSTEM');
