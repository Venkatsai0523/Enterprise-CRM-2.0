import React from 'react';
import { ViewMode } from '../types';

interface SidebarProps {
  currentView: ViewMode;
  onNavigate: (view: ViewMode) => void;
  unreadCount: number;
  isOpenMobile: boolean;
  onCloseMobile: () => void;
}

export const Sidebar: React.FC<SidebarProps> = ({
  currentView,
  onNavigate,
  unreadCount,
  isOpenMobile,
  onCloseMobile,
}) => {
  const navItems: { id: ViewMode; label: string; icon: string; badge?: number }[] = [
    { id: 'dashboard', label: 'Dashboard', icon: 'dashboard' },
    { id: 'customers', label: 'Customers', icon: 'groups' },
    { id: 'leads', label: 'Leads', icon: 'person_add' },
    { id: 'pipeline', label: 'Pipeline', icon: 'account_tree' },
    { id: 'tasks', label: 'Tasks', icon: 'task_alt' },
    { id: 'analytics', label: 'Analytics', icon: 'analytics' },
    { id: 'notifications', label: 'Notifications', icon: 'notifications', badge: unreadCount },
  ];

  return (
    <>
      {/* Mobile Backdrop */}
      {isOpenMobile && (
        <div
          className="fixed inset-0 bg-black/40 z-40 md:hidden"
          onClick={onCloseMobile}
        />
      )}

      <nav
        className={`bg-sidebar-bg fixed left-0 top-0 h-screen w-sidebar-width border-r border-border-subtle flex flex-col py-4 px-3 z-50 transition-transform duration-300 md:translate-x-0 ${
          isOpenMobile ? 'translate-x-0' : '-translate-x-full md:translate-x-0'
        }`}
      >
        {/* Brand Header */}
        <div className="mb-6 px-3 flex items-center gap-3">
          <div className="w-8 h-8 rounded bg-primary text-on-primary flex items-center justify-center font-bold text-sm shadow-sm">
            <span className="material-symbols-outlined text-[20px]" data-icon="hub">
              hub
            </span>
          </div>
          <div>
            <h1 className="font-semibold text-base text-on-surface leading-none">NexusCRM</h1>
            <span className="text-[11px] font-semibold tracking-wider text-on-surface-variant uppercase">
              Enterprise
            </span>
          </div>
        </div>

        {/* Navigation List */}
        <ul className="flex flex-col gap-1 flex-1 text-sm font-medium">
          {navItems.map((item) => {
            const isActive =
              currentView === item.id ||
              (item.id === 'customers' && currentView === 'customer-detail');

            return (
              <li key={item.id}>
                <button
                  onClick={() => {
                    onNavigate(item.id);
                    onCloseMobile();
                  }}
                  className={`w-full flex items-center justify-between px-3 py-2 rounded-lg transition-all duration-150 ${
                    isActive
                      ? 'bg-primary-container text-on-primary-container font-semibold shadow-xs'
                      : 'text-on-surface-variant hover:bg-surface-container-low hover:text-on-surface'
                  }`}
                >
                  <div className="flex items-center gap-3">
                    <span
                      className={`material-symbols-outlined text-[20px] ${
                        isActive ? 'filled' : ''
                      }`}
                    >
                      {item.icon}
                    </span>
                    <span>{item.label}</span>
                  </div>
                  {item.badge && item.badge > 0 ? (
                    <span className="bg-primary text-on-primary text-[10px] font-bold px-1.5 py-0.5 rounded-full">
                      {item.badge}
                    </span>
                  ) : null}
                </button>
              </li>
            );
          })}
        </ul>

        {/* Settings & Bottom Profile */}
        <div className="mt-auto border-t border-border-subtle pt-3 flex flex-col gap-1">
          <button
            onClick={() => {
              onNavigate('settings');
              onCloseMobile();
            }}
            className={`w-full flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-all ${
              currentView === 'settings'
                ? 'bg-primary-container text-on-primary-container font-semibold'
                : 'text-on-surface-variant hover:bg-surface-container-low hover:text-on-surface'
            }`}
          >
            <span className="material-symbols-outlined text-[20px]">settings</span>
            <span>Settings</span>
          </button>

          <button
            onClick={() => {
              onNavigate('login');
              onCloseMobile();
            }}
            className="mt-2 p-2 rounded-lg hover:bg-surface-container-low flex items-center gap-2 text-left transition-colors w-full group"
          >
            <img
              src="https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=150&auto=format&fit=crop&q=80"
              alt="Admin User"
              className="w-8 h-8 rounded-full object-cover border border-border-subtle"
            />
            <div className="flex-1 min-w-0">
              <p className="text-xs font-semibold text-on-surface truncate group-hover:text-primary">
                Admin User
              </p>
              <p className="text-[11px] text-on-surface-variant truncate">
                admin@nexuscrm.com
              </p>
            </div>
            <span className="material-symbols-outlined text-[16px] text-on-surface-variant">
              logout
            </span>
          </button>
        </div>
      </nav>
    </>
  );
};
