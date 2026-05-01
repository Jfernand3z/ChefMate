export interface RecipeIngredient {
    name: string;
    quantity: number;
    unit: string;
}

export interface Recipe {
    id?: string;
    name: string;
    type: string;
    description: string;
    ingredients: RecipeIngredient[];
    steps: string[];
    prep_time_minutes: number;
    location?: string;
    created_at?: string;
    max_servings?: number;
}

export interface RecipeGenerateParams {
    servings: number;
    location?: string;
    selected_products?: string[];
    priority_product?: string;
    recipe_type?: string;
}
