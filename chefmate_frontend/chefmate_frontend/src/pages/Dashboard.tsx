import React, { useState } from 'react';
import Layout from '../features/dashboard/Layout';
import ProductManager from '../features/products/ProductManager';
import ProductAnalytics from '../features/products/ProductAnalytics';
import UserMetrics from '../features/users/UserMetrics';
import RecipeGenerator from '../features/recipes/RecipeGenerator';
import RecipeHistory from '../features/recipes/RecipeHistory';

export default function Dashboard() {
  const [activeTab, setActiveTab] = useState('products');

  const renderActiveTab = () => {
    switch (activeTab) {
      case 'products':
        return <ProductManager />;
      case 'analytics':
        return <ProductAnalytics />;
      case 'recipes':
        return <RecipeGenerator />;
      case 'recipe-history':
        return <RecipeHistory />;
      case 'metrics':
        return <UserMetrics />;
      default:
        return <ProductManager />;
    }
  };

  return (
    <Layout activeTab={activeTab} setActiveTab={setActiveTab}>
        <div className="animate-in fade-in slide-in-from-bottom-4 duration-500">
            {renderActiveTab()}
        </div>
    </Layout>
  );
}
