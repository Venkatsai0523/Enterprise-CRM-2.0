import React from 'react';
import { ViewMode } from '../types';

interface TopBarProps {
  currentView: ViewMode;
  onNavigate: (view: ViewMode) => void;
  unreadCount: number;
  onOpenMobile: () => void;
  searchTerm: string;
  onSearchChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
  currentUser?: any;
}

export const TopBar: React.FC<TopBarProps> = ({
  currentView,
  onNavigate,
  unreadCount,
  onOpenMobile,
  searchTerm,
  onSearchChange,
  currentUser,
}) => {
  const getPageTitle = () => {
    switch (currentView) {
      case 'dashboard':
        return 'Overview Dashboard';
      case 'customers':
        return 'Customers Directory';
      case 'customer-detail':
        return 'Customer Profile';
      case 'leads':
        return 'Lead Management';
      case 'pipeline':
        return 'Sales Pipeline';
      case 'tasks':
        return 'Tasks & Agenda';
      case 'analytics':
        return 'Analytics & Reports';
      case 'settings':
        return 'Organization & User Management';
      case 'notifications':
        return 'Notifications Center';
      case 'org-setup':
        return 'Organization Setup';
      default:
        return 'NexusCRM';
    }
  };

  return (
    <header className="h-16 border-b border-border-subtle bg-surface-container-lowest sticky top-0 z-30 px-4 md:px-6 flex items-center justify-between">
      {/* Left Title & Mobile Menu Button */}
      <div className="flex items-center gap-3">
        <button
          onClick={onOpenMobile}
          className="p-2 -ml-2 text-on-surface-variant hover:text-on-surface hover:bg-surface-container-low rounded-lg md:hidden"
          title="Open Menu"
        >
          <span className="material-symbols-outlined text-[24px]">menu</span>
        </button>

        <div>
          <h2 className="text-base md:text-lg font-bold text-on-surface leading-tight">
            {getPageTitle()}
          </h2>
          {currentView === 'customer-detail' && (
            <div className="flex items-center gap-1.5 text-xs text-on-surface-variant">
              <button
                onClick={() => onNavigate('customers')}
                className="hover:underline text-primary"
              >
                Customers
              </button>
              <span>/</span>
              <span>Acme Corp</span>
            </div>
          )}
        </div>
      </div>

      {/* Right Search & Controls */}
      <div className="flex items-center gap-3">
        {/* Global Search Bar */}
        <div className="relative hidden sm:block w-64 lg:w-80">
          <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-on-surface-variant text-[18px]">
            search
          </span>
          <input
            type="text"
            value={searchTerm}
            onChange={onSearchChange}
            placeholder="Search deals, leads, clients..."
            className="w-full bg-surface-container-low text-on-surface text-xs pl-9 pr-8 py-2 rounded-lg border border-transparent focus:border-primary focus:bg-surface-container-lowest focus:outline-none transition-all"
          />
          <span className="absolute right-2.5 top-1/2 -translate-y-1/2 text-[10px] bg-surface-container-high text-on-surface-variant px-1.5 py-0.5 rounded font-mono">
            ⌘K
          </span>
        </div>

        {/* Notifications Icon Button */}
        <button
          onClick={() => onNavigate('notifications')}
          className="relative p-2 text-on-surface-variant hover:text-on-surface hover:bg-surface-container-low rounded-lg transition-colors"
          title="Notifications"
        >
          <span className="material-symbols-outlined text-[20px]">notifications</span>
          {unreadCount > 0 && (
            <span className="absolute top-1.5 right-1.5 w-2 h-2 rounded-full bg-red-500 ring-2 ring-surface-container-lowest" />
          )}
        </button>

        {/* Help */}
        <button
          className="p-2 text-on-surface-variant hover:text-on-surface hover:bg-surface-container-low rounded-lg transition-colors hidden sm:flex"
          title="Help & Support"
        >
          <span className="material-symbols-outlined text-[20px]">help</span>
        </button>

        {/* User Profile Thumbnail */}
        <button
          onClick={() => onNavigate('settings')}
          className="flex items-center gap-2 pl-2 border-l border-border-subtle hover:opacity-80 transition-opacity"
        >
          {currentUser ? (
            <div className="w-8 h-8 rounded-full bg-primary text-on-primary flex items-center justify-center text-[10px] font-extrabold ring-2 ring-primary/20">
              {`${currentUser.firstName?.[0] || ''}${currentUser.lastName?.[0] || ''}`}
            </div>
          ) : (
            <img
              src="https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=150&auto=format&fit=crop&q=80"
              alt="User"
              className="w-8 h-8 rounded-full object-cover ring-2 ring-primary/20"
            />
          )}
        </button>
      </div>
    </header>
  );
};
