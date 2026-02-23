package com.dietary.food.config;

import com.dietary.food.domain.Food;
import com.dietary.food.domain.FoodSource;
import com.dietary.food.repository.FoodRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class FoodDataInitializer {

    @Bean
    @Profile("dev")
    public CommandLineRunner initSystemFoods(FoodRepository foodRepository) {
        return args -> {
            if (foodRepository.findAllSystemFoods().isEmpty()) {
                log.info("Seeding system foods...");

                List<Food> systemFoods = List.of(
                        // Proteins
                        createSystemFood("Chicken Breast (Grilled)", "100", "g", 165,
                                new BigDecimal("31"), new BigDecimal("0"), new BigDecimal("3.6"), "Protein"),
                        createSystemFood("Salmon (Baked)", "100", "g", 208,
                                new BigDecimal("20"), new BigDecimal("0"), new BigDecimal("13"), "Protein"),
                        createSystemFood("Eggs (Whole, Boiled)", "1", "large", 78,
                                new BigDecimal("6.3"), new BigDecimal("0.6"), new BigDecimal("5.3"), "Protein"),
                        createSystemFood("Greek Yogurt (Plain, Non-fat)", "170", "g", 100,
                                new BigDecimal("17"), new BigDecimal("6"), new BigDecimal("0.7"), "Dairy"),
                        createSystemFood("Cottage Cheese (Low-fat)", "100", "g", 72,
                                new BigDecimal("12"), new BigDecimal("2.7"), new BigDecimal("1"), "Dairy"),

                        // Carbohydrates
                        createSystemFood("Brown Rice (Cooked)", "100", "g", 112,
                                new BigDecimal("2.6"), new BigDecimal("24"), new BigDecimal("0.9"), "Grains"),
                        createSystemFood("Oatmeal (Cooked)", "100", "g", 71,
                                new BigDecimal("2.5"), new BigDecimal("12"), new BigDecimal("1.5"), "Grains"),
                        createSystemFood("Sweet Potato (Baked)", "100", "g", 90,
                                new BigDecimal("2"), new BigDecimal("21"), new BigDecimal("0.1"), "Vegetables"),
                        createSystemFood("Quinoa (Cooked)", "100", "g", 120,
                                new BigDecimal("4.4"), new BigDecimal("21"), new BigDecimal("1.9"), "Grains"),
                        createSystemFood("Whole Wheat Bread", "1", "slice", 81,
                                new BigDecimal("4"), new BigDecimal("14"), new BigDecimal("1.1"), "Grains"),

                        // Vegetables
                        createSystemFood("Broccoli (Steamed)", "100", "g", 35,
                                new BigDecimal("2.4"), new BigDecimal("7"), new BigDecimal("0.4"), "Vegetables"),
                        createSystemFood("Spinach (Raw)", "100", "g", 23,
                                new BigDecimal("2.9"), new BigDecimal("3.6"), new BigDecimal("0.4"), "Vegetables"),
                        createSystemFood("Avocado", "100", "g", 160,
                                new BigDecimal("2"), new BigDecimal("9"), new BigDecimal("15"), "Vegetables"),
                        createSystemFood("Tomatoes (Raw)", "100", "g", 18,
                                new BigDecimal("0.9"), new BigDecimal("3.9"), new BigDecimal("0.2"), "Vegetables"),
                        createSystemFood("Carrots (Raw)", "100", "g", 41,
                                new BigDecimal("0.9"), new BigDecimal("10"), new BigDecimal("0.2"), "Vegetables"),

                        // Fruits
                        createSystemFood("Banana", "1", "medium", 105,
                                new BigDecimal("1.3"), new BigDecimal("27"), new BigDecimal("0.4"), "Fruits"),
                        createSystemFood("Apple", "1", "medium", 95,
                                new BigDecimal("0.5"), new BigDecimal("25"), new BigDecimal("0.3"), "Fruits"),
                        createSystemFood("Blueberries", "100", "g", 57,
                                new BigDecimal("0.7"), new BigDecimal("14"), new BigDecimal("0.3"), "Fruits"),
                        createSystemFood("Orange", "1", "medium", 62,
                                new BigDecimal("1.2"), new BigDecimal("15"), new BigDecimal("0.2"), "Fruits"),
                        createSystemFood("Strawberries", "100", "g", 32,
                                new BigDecimal("0.7"), new BigDecimal("8"), new BigDecimal("0.3"), "Fruits"),

                        // Nuts & Seeds
                        createSystemFood("Almonds", "28", "g", 164,
                                new BigDecimal("6"), new BigDecimal("6"), new BigDecimal("14"), "Nuts & Seeds"),
                        createSystemFood("Walnuts", "28", "g", 185,
                                new BigDecimal("4.3"), new BigDecimal("4"), new BigDecimal("18"), "Nuts & Seeds"),
                        createSystemFood("Chia Seeds", "28", "g", 137,
                                new BigDecimal("4.4"), new BigDecimal("12"), new BigDecimal("9"), "Nuts & Seeds"),

                        // Oils & Fats
                        createSystemFood("Olive Oil", "1", "tbsp", 119,
                                new BigDecimal("0"), new BigDecimal("0"), new BigDecimal("14"), "Oils & Fats"),
                        createSystemFood("Coconut Oil", "1", "tbsp", 121,
                                new BigDecimal("0"), new BigDecimal("0"), new BigDecimal("13"), "Oils & Fats"));

                foodRepository.saveAll(systemFoods);
                log.info("Seeded {} system foods", systemFoods.size());
            }
        };
    }

    private Food createSystemFood(String name, String servingSize, String servingUnit,
            int calories, BigDecimal protein, BigDecimal carbs,
            BigDecimal fat, String category) {
        return Food.builder()
                .name(name)
                .source(FoodSource.SYSTEM)
                .servingSize(servingSize)
                .servingUnit(servingUnit)
                .caloriesPerServing(calories)
                .proteinGrams(protein)
                .carbsGrams(carbs)
                .fatGrams(fat)
                .category(category)
                .active(true)
                .build();
    }
}
