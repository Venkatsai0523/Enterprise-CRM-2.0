import React, { useState } from 'react';
import { NotificationItem, ViewMode } from '../types';

interface NotificationsViewProps {
  notifications: NotificationItem[];
  onMarkAllRead: () => void;
  onNavigate: (view: ViewMode) => void;
}

export const NotificationsView: React.FC<NotificationsViewProps> = ({
  notifications,
  onMarkAllRead,
  onNavigate,
}) => {
  const [activeTab, setActiveTab] = useState<'All' | 'Unread' | 'Mentions' | 'System'>('All');

  const filteredNotifs = notifications.filter((n) => {
    if (activeTab === 'Unread') return !n.read;
    if (activeTab === 'Mentions') return n.category === 'Mentions' || n.type === 'mention';
    if (activeTab === 'System') return n.category === 'System' || n.type === 'system';
    return true;
  });

  return (
    <div className="p-4 md:p-6 max-w-4xl mx-auto space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold text-on-surface">Notifications Center</h1>
          <p className="text-xs text-on-surface-variant mt-0.5">
            Stay updated on deal milestones, team mentions, and system events
          </p>
        </div>
        <button
          onClick={onMarkAllRead}
          className="bg-surface-container-lowest hover:bg-surface-container border border-border-subtle text-xs font-semibold text-primary px-3.5 py-2 rounded-lg flex items-center gap-1.5 shadow-xs transition-colors"
        >
          <span className="material-symbols-outlined text-[16px]">done_all</span>
          <span>Mark All as Read</span>
        </button>
      </div>

      {/* Filter Tabs */}
      <div className="bg-surface-container-lowest p-3 rounded-2xl border border-border-subtle shadow-xs flex items-center gap-1.5 overflow-x-auto">
        {(['All', 'Unread', 'Mentions', 'System'] as const).map((tab) => (
          <button
            key={tab}
            onClick={() => setActiveTab(tab)}
            className={`px-4 py-2 rounded-xl text-xs font-bold transition-all ${
              activeTab === tab
                ? 'bg-primary text-on-primary shadow-xs'
                : 'text-on-surface-variant hover:bg-surface-container-low'
            }`}
          >
            {tab}
          </button>
        ))}
      </div>

      {/* Notifications List */}
      <div className="space-y-3">
        {filteredNotifs.length === 0 ? (
          <div className="bg-surface-container-lowest p-8 rounded-2xl border border-border-subtle text-center text-xs text-on-surface-variant">
            No notifications in this category.
          </div>
        ) : (
          filteredNotifs.map((item) => (
            <div
              key={item.id}
              className={`p-4 rounded-2xl border transition-all flex items-start gap-3.5 ${
                !item.read
                  ? 'bg-surface-container-low/70 border-primary/30 shadow-xs'
                  : 'bg-surface-container-lowest border-border-subtle opacity-85'
              }`}
            >
              {item.userAvatar ? (
                <img
                  src={item.userAvatar}
                  alt=""
                  className="w-10 h-10 rounded-full object-cover shrink-0 ring-2 ring-primary/20"
                />
              ) : (
                <div className="w-10 h-10 rounded-full bg-primary/10 text-primary flex items-center justify-center shrink-0 font-bold">
                  <span className="material-symbols-outlined text-[20px]">
                    {item.iconName || 'notifications'}
                  </span>
                </div>
              )}

              <div className="flex-1 min-w-0 space-y-1">
                <div className="flex items-center justify-between gap-2">
                  <h3 className="text-xs font-bold text-on-surface truncate">{item.title}</h3>
                  <span className="text-[10px] text-on-surface-variant shrink-0">{item.time}</span>
                </div>
                <p className="text-xs text-on-surface-variant leading-relaxed">{item.body}</p>

                <div className="pt-2 flex items-center gap-3 text-xs font-bold">
                  {item.type === 'deal' && (
                    <button
                      onClick={() => onNavigate('pipeline')}
                      className="text-primary hover:underline flex items-center gap-1"
                    >
                      <span>View Deal Pipeline</span>
                      <span className="material-symbols-outlined text-[14px]">arrow_forward</span>
                    </button>
                  )}
                  {item.type === 'mention' && (
                    <button
                      onClick={() => onNavigate('tasks')}
                      className="text-primary hover:underline flex items-center gap-1"
                    >
                      <span>Reply</span>
                    </button>
                  )}
                </div>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
};
