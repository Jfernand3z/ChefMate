import React, { useState, useEffect } from 'react';
import { api } from '../../services/api';
import { Sparkles, ChefHat, Clock, Users, MapPin, Star, Filter, Loader2, CookingPot, CheckCircle2, ChevronDown, ChevronUp, ListChecks } from 'lucide-react';
import type { Recipe, RecipeGenerateParams } from './types';

interface Product {
    id: string;
    name: string;
    quantity: number;
    unit: string;
    expiration_date?: string;
}


export default function RecipeGenerator() {
    const [recipes, setRecipes] = useState<Recipe[]>([]);
    const [products, setProducts] = useState<Product[]>([]);
    const [loading, setLoading] = useState(false);
    const [cookingId, setCookingId] = useState<number | null>(null);
    const [cookedIndexes, setCookedIndexes] = useState<Set<number>>(new Set());
    const [expandedIndex, setExpandedIndex] = useState<number | null>(null);
    const [showFilters, setShowFilters] = useState(false);
    const [generated, setGenerated] = useState(false);

    const [params, setParams] = useState<RecipeGenerateParams>({
        servings: 2,
        location: '',
        selected_products: [],
        priority_product: '',
        recipe_type: '',
    });

    const loadingTexts = [
        "Afilando los cuchillos...",
        "Buscando ingredientes secretos...",
        "Calentando los fogones...",
        "Consultando el recetario de la abuela...",
        "Añadiendo una pizca de magia...",
        "Calculando tiempos de cocción..."
    ];
    const [loadingTextIndex, setLoadingTextIndex] = useState(0);

    useEffect(() => {
        let interval: NodeJS.Timeout;
        if (loading) {
            interval = setInterval(() => {
                setLoadingTextIndex(prev => (prev + 1) % loadingTexts.length);
            }, 2500);
        }
        return () => clearInterval(interval);
    }, [loading]);

    useEffect(() => {
        api.get('/products').then(res => {
            const list: Product[] = res.data.products || res.data || [];
            const today = new Date();
            today.setHours(0, 0, 0, 0);
            const vigentes = list.filter(p => {
                if (p.quantity <= 0) return false;
                if (!p.expiration_date) return true;
                return new Date(p.expiration_date) > today;
            });
            setProducts(vigentes);
        }).catch(() => {});
    }, []);


    const handleGenerate = async () => {
        setLoading(true);
        setRecipes([]);
        setGenerated(false);
        setCookedIndexes(new Set());
        setExpandedIndex(null);
        try {
            const payload: any = { servings: params.servings };
            if (params.location) payload.location = params.location;
            if (params.selected_products && params.selected_products.length > 0) payload.selected_products = params.selected_products;
            if (params.priority_product) payload.priority_product = params.priority_product;
            if (params.recipe_type) payload.recipe_type = params.recipe_type;

            const res = await api.post('/recipes/generate', payload);
            setRecipes(Array.isArray(res.data) ? res.data : []);
            setGenerated(true);
        } catch (e: any) {
            alert(e?.response?.data?.detail || 'Error al generar recetas. Verifica que Ollama esté activo.');
        } finally {
            setLoading(false);
        }
    };

    const handleCook = async (recipe: Recipe, index: number) => {
        setCookingId(index);
        try {
            await api.post('/recipes/cook', { recipe, servings: params.servings });
            setCookedIndexes(prev => new Set([...prev, index]));
        } catch (e: any) {
            alert(e?.response?.data?.detail || 'Error al cocinar la receta.');
        } finally {
            setCookingId(null);
        }
    };

    const toggleProduct = (name: string) => {
        setParams(prev => {
            const current = prev.selected_products || [];
            const updated = current.includes(name)
                ? current.filter(p => p !== name)
                : [...current, name];
            const newPriority = updated.includes(prev.priority_product || '') ? prev.priority_product : '';
            return { ...prev, selected_products: updated, priority_product: newPriority };
        });
    };

    const togglePriority = (name: string) => {
        setParams(prev => ({
            ...prev,
            priority_product: prev.priority_product === name ? '' : name,
        }));
    };

    const hasFilters = !!(params.location || (params.selected_products && params.selected_products.length > 0) || params.priority_product || params.recipe_type);

    return (
        <div className="w-full">
            {/* Header */}
            <div className="flex justify-between items-end mb-8">
                <div>
                    <h1 className="text-4xl font-heading font-bold text-text-primary mb-2">Chef IA</h1>
                    <p className="text-text-secondary">Genera recetas inteligentes según tu inventario disponible.</p>
                </div>
                <button
                    onClick={() => setShowFilters(v => !v)}
                    className={`flex items-center gap-2 px-5 py-3 rounded-xl font-semibold border transition-all duration-200 ${hasFilters ? 'bg-accent text-white border-accent shadow-lg shadow-accent/20' : 'border-border-medium text-text-secondary hover:border-primary hover:text-primary bg-surface'}`}
                >
                    <Filter size={18} />
                    Filtros {hasFilters && `(activos)`}
                </button>
            </div>

            {/* Filter Panel */}
            {showFilters && (
                <div className="bg-surface border border-border-light rounded-2xl p-6 mb-6 shadow-sm animate-in fade-in slide-in-from-top-2 duration-200">
                    <h3 className="text-sm font-bold text-text-secondary uppercase tracking-wider mb-5 flex items-center gap-2">
                        <ListChecks size={16} /> Parametrización del Prompt
                    </h3>
                    <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-5">
                        {/* Location */}
                        <div>
                            <label className="text-xs font-bold text-text-secondary uppercase tracking-wider mb-1.5 block flex items-center gap-1.5">
                                <MapPin size={12} /> Ubicación
                            </label>
                            <input
                                type="text"
                                placeholder="Ej: La Paz, Cochabamba…"
                                value={params.location}
                                onChange={e => setParams(p => ({ ...p, location: e.target.value }))}
                                className="w-full bg-background border border-border-medium focus:border-primary px-4 py-2.5 rounded-xl outline-none transition-colors text-sm"
                            />
                        </div>

                        {/* Recipe Type */}
                        <div>
                            <label className="text-xs font-bold text-text-secondary uppercase tracking-wider mb-1.5 block">
                                Tipo de Receta
                            </label>
                            <select
                                value={params.recipe_type}
                                onChange={e => setParams(p => ({ ...p, recipe_type: e.target.value }))}
                                className="w-full bg-background border border-border-medium focus:border-primary px-4 py-2.5 rounded-xl outline-none transition-colors text-sm"
                            >
                                <option value="">Cualquiera</option>
                                <option value="plato">Plato</option>
                                <option value="reposteria">Repostería</option>
                            </select>
                        </div>

                        {/* Servings */}
                        <div>
                            <label className="text-xs font-bold text-text-secondary uppercase tracking-wider mb-1.5 block flex items-center gap-1.5">
                                <Users size={12} /> Porciones <span className="text-danger">*</span>
                            </label>
                            <input
                                type="number"
                                min={1}
                                value={params.servings}
                                onChange={e => setParams(p => ({ ...p, servings: Math.max(1, parseInt(e.target.value) || 1) }))}
                                className="w-full bg-background border border-border-medium focus:border-primary px-4 py-2.5 rounded-xl outline-none transition-colors text-sm"
                            />
                        </div>
                    </div>

                    {/* Unified product chips section */}
                    {products.length > 0 && (
                        <div className="mt-5 space-y-4">
                            {/* Row 1: include */}
                            <div>
                                <label className="text-xs font-bold text-text-secondary uppercase tracking-wider mb-2 block">
                                    Ingredientes a considerar
                                    <span className="ml-1.5 font-normal text-text-muted">(vacío = todos)</span>
                                </label>
                                <div className="flex flex-wrap gap-2">
                                    {products.map(prod => {
                                        const selected = (params.selected_products || []).includes(prod.name);
                                        return (
                                            <button
                                                key={prod.id}
                                                type="button"
                                                onClick={() => toggleProduct(prod.name)}
                                                className={`px-3 py-1.5 rounded-full text-sm font-medium border transition-all duration-150 ${
                                                    selected
                                                        ? 'bg-primary text-white border-primary shadow-sm shadow-primary/20'
                                                        : 'bg-surface border-border-medium text-text-secondary hover:border-primary hover:text-primary'
                                                }`}
                                            >
                                                {prod.name}
                                            </button>
                                        );
                                    })}
                                </div>
                            </div>

                            {/* Row 2: priority — chips from products (filtered to selection if active) */}
                            <div>
                                <label className="text-xs font-bold text-text-secondary uppercase tracking-wider mb-2 block flex items-center gap-1.5">
                                    <Star size={12} className="text-accent" />
                                    Ingrediente prioritario
                                    <span className="ml-1.5 font-normal text-text-muted">(la IA lo usará en la mayoría de recetas)</span>
                                </label>
                                <div className="flex flex-wrap gap-2">
                                    {(params.selected_products && params.selected_products.length > 0
                                        ? products.filter(p => (params.selected_products || []).includes(p.name))
                                        : products
                                    ).map(prod => {
                                        const isPriority = params.priority_product === prod.name;
                                        return (
                                            <button
                                                key={prod.id}
                                                type="button"
                                                onClick={() => togglePriority(prod.name)}
                                                className={`flex items-center gap-1.5 px-3 py-1.5 rounded-full text-sm font-medium border transition-all duration-150 ${
                                                    isPriority
                                                        ? 'bg-accent text-white border-accent shadow-sm shadow-accent/20 scale-105'
                                                        : 'bg-surface border-border-medium text-text-secondary hover:border-accent hover:text-accent'
                                                }`}
                                            >
                                                {isPriority && <Star size={11} className="fill-white" />}
                                                {prod.name}
                                            </button>
                                        );
                                    })}
                                </div>
                            </div>
                        </div>
                    )}
                </div>
            )}

            {/* Servings quick-set (when filters hidden) */}
            {!showFilters && (
                <div className="flex items-center gap-4 mb-6">
                    <label className="text-sm font-semibold text-text-secondary flex items-center gap-2">
                        <Users size={16} /> Porciones:
                    </label>
                    <input
                        type="number"
                        min={1}
                        value={params.servings}
                        onChange={e => setParams(p => ({ ...p, servings: Math.max(1, parseInt(e.target.value) || 1) }))}
                        className="w-24 bg-surface border border-border-medium focus:border-primary px-4 py-2 rounded-xl outline-none transition-colors text-sm font-bold text-center"
                    />
                </div>
            )}

            {/* Generate Button */}
            <button
                id="generate-recipes-btn"
                onClick={handleGenerate}
                disabled={loading}
                className="w-full flex items-center justify-center gap-3 bg-gradient-to-r from-primary to-info text-white py-4 rounded-2xl font-bold text-lg shadow-xl shadow-primary/20 hover:shadow-primary/40 transition-all hover:scale-[1.01] active:scale-[0.99] disabled:opacity-60 disabled:cursor-not-allowed mb-10"
            >
                {loading ? (
                    <>
                        <Loader2 size={22} className="animate-spin" />
                        Consultando al Chef IA…
                    </>
                ) : (
                    <>
                        <Sparkles size={22} />
                        Generar Recetas con IA
                    </>
                )}
            </button>

            {/* Loading state */}
            {loading && (
                <div className="flex flex-col items-center justify-center py-20 text-text-muted gap-6 animate-in fade-in zoom-in duration-300">
                    <div className="relative">
                        <CookingPot size={72} strokeWidth={1.5} className="text-primary animate-bounce" />
                        <Sparkles size={24} className="text-accent absolute -top-2 -right-2 animate-spin" style={{ animationDuration: '3s' }} />
                    </div>
                    <div className="text-center">
                        <p className="text-xl font-bold text-text-primary mb-2 transition-all duration-300">
                            {loadingTexts[loadingTextIndex]}
                        </p>
                        <p className="text-sm text-text-secondary">Analizando tus ingredientes para encontrar la mejor combinación culinaria.</p>
                    </div>
                </div>
            )}

            {/* Empty state */}
            {!loading && !generated && (
                <div className="flex flex-col items-center justify-center py-20 text-text-muted gap-4">
                    <ChefHat size={64} strokeWidth={1} className="text-border-medium" />
                    <p className="text-lg">Configura las porciones y genera tus recetas.</p>
                </div>
            )}

            {/* Recipe Cards */}
            {generated && recipes.length === 0 && !loading && (
                <div className="flex flex-col items-center justify-center py-20 gap-4 text-text-muted">
                    <ChefHat size={56} strokeWidth={1} className="text-border-medium" />
                    <p>La IA no pudo generar recetas con el inventario actual.</p>
                </div>
            )}

            <div className="grid grid-cols-1 xl:grid-cols-2 gap-6">
                {recipes.map((recipe, idx) => {
                    const isExpanded = expandedIndex === idx;
                    const isCooking = cookingId === idx;
                    const isCooked = cookedIndexes.has(idx);

                    return (
                        <div
                            key={idx}
                            className={`bg-surface rounded-2xl border transition-all duration-300 overflow-hidden shadow-sm ${isCooked ? 'border-primary/40 bg-primary-light/10' : 'border-border-light hover:border-primary/30 hover:shadow-md'}`}
                        >
                            {/* Card Header */}
                            <div className="p-6">
                                <div className="flex items-start justify-between gap-4 mb-3">
                                    <div className="flex items-center gap-3">
                                        <div className={`p-2.5 rounded-xl ${recipe.type === 'reposteria' ? 'bg-accent-light text-accent' : 'bg-primary-light text-primary'}`}>
                                            <ChefHat size={20} strokeWidth={2} />
                                        </div>
                                        <div>
                                            <h3 className="text-lg font-bold text-text-primary leading-tight">{recipe.name}</h3>
                                            <span className={`text-xs font-semibold px-2 py-0.5 rounded-full inline-block mt-0.5 ${recipe.type === 'reposteria' ? 'bg-accent-light text-accent' : 'bg-primary-light text-primary'}`}>
                                                {recipe.type || 'plato'}
                                            </span>
                                        </div>
                                    </div>
                                    <div className="flex items-center gap-1.5 text-text-muted shrink-0">
                                        <Clock size={14} />
                                        <span className="text-sm">{recipe.prep_time_minutes} min</span>
                                    </div>
                                </div>

                                <p className="text-text-secondary text-sm leading-relaxed mb-4">{recipe.description}</p>

                                {/* Ingredients summary */}
                                <div className="flex flex-wrap gap-1.5 mb-4">
                                    {recipe.ingredients.slice(0, 4).map((ing, i) => (
                                        <span key={i} className="text-xs px-2 py-1 bg-surface-secondary text-text-secondary rounded-lg border border-border-light">
                                            {ing.name} · {ing.quantity}{ing.unit}
                                        </span>
                                    ))}
                                    {recipe.ingredients.length > 4 && (
                                        <span className="text-xs px-2 py-1 bg-surface-secondary text-text-muted rounded-lg border border-border-light">
                                            +{recipe.ingredients.length - 4} más
                                        </span>
                                    )}
                                </div>

                                {/* Actions */}
                                <div className="flex gap-3">
                                    <button
                                        onClick={() => setExpandedIndex(isExpanded ? null : idx)}
                                        className="flex-1 flex items-center justify-center gap-2 py-2.5 rounded-xl border border-border-medium text-text-secondary hover:border-primary hover:text-primary transition-all text-sm font-semibold"
                                    >
                                        {isExpanded ? <ChevronUp size={16} /> : <ChevronDown size={16} />}
                                        {isExpanded ? 'Ocultar' : 'Ver receta'}
                                    </button>
                                    <button
                                        id={`cook-new-recipe-${idx}`}
                                        onClick={() => !isCooked && handleCook(recipe, idx)}
                                        disabled={isCooking || isCooked}
                                        className={`flex-1 flex items-center justify-center gap-2 py-2.5 rounded-xl font-bold text-sm transition-all ${isCooked ? 'bg-primary-light text-primary cursor-default' : 'bg-primary hover:bg-primary-hover text-white shadow-lg shadow-primary/20 hover:scale-[1.02] active:scale-[0.98] disabled:opacity-60 disabled:cursor-not-allowed'}`}
                                    >
                                        {isCooking ? (
                                            <><Loader2 size={16} className="animate-spin" /> Cocinando…</>
                                        ) : isCooked ? (
                                            <><CheckCircle2 size={16} /> ¡Cocinado!</>
                                        ) : (
                                            <><CookingPot size={16} /> Cocinar</>
                                        )}
                                    </button>
                                </div>
                            </div>

                            {/* Expanded Steps */}
                            {isExpanded && (
                                <div className="border-t border-border-light p-6 bg-background animate-in fade-in slide-in-from-top-1 duration-200">
                                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                                        <div>
                                            <h4 className="text-sm font-bold text-text-primary uppercase tracking-wider mb-3">Ingredientes completos</h4>
                                            <ul className="space-y-2">
                                                {recipe.ingredients.map((ing, i) => (
                                                    <li key={i} className="flex justify-between items-center text-sm py-2 border-b border-border-light last:border-0">
                                                        <span className="text-text-primary font-medium">{ing.name}</span>
                                                        <span className="text-text-secondary font-mono">{ing.quantity} {ing.unit}</span>
                                                    </li>
                                                ))}
                                            </ul>
                                        </div>
                                        <div>
                                            <h4 className="text-sm font-bold text-text-primary uppercase tracking-wider mb-3">Pasos</h4>
                                            <ol className="space-y-3">
                                                {recipe.steps.map((step, i) => (
                                                    <li key={i} className="flex gap-3 text-sm text-text-secondary">
                                                        <span className="flex-shrink-0 w-6 h-6 rounded-full bg-primary-light text-primary font-bold text-xs flex items-center justify-center">{i + 1}</span>
                                                        <span>{step}</span>
                                                    </li>
                                                ))}
                                            </ol>
                                        </div>
                                    </div>
                                </div>
                            )}
                        </div>
                    );
                })}
            </div>
        </div>
    );
}
