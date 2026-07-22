import React, { useState } from 'react';
import { Customer, Task, Activity, ViewMode } from '../types';

interface CustomerDetailViewProps {
  customer: Customer;
  activities: Activity[];
  tasks: Task[];
  onNavigate: (view: ViewMode) => void;
  onAddActivity: (activity: Activity) => void;
  onOpenTaskModal: () => void;
}

export const CustomerDetailView: React.FC<CustomerDetailViewProps> = ({
  customer,
  activities,
  tasks,
  onNavigate,
  onAddActivity,
  onOpenTaskModal,
}) => {
  const [activeTab, setActiveTab] = useState<'activity' | 'contacts' | 'deals' | 'tickets'>('activity');
  const [noteContent, setNoteContent] = useState('');
  const [noteType, setNoteType] = useState<'call' | 'email' | 'meeting'>('call');

  const customerTasks = tasks.filter(
    (t) => t.deal.toLowerCase().includes(customer.name.toLowerCase()) || t.name.toLowerCase().includes('acme')
  );

  const handlePostActivity = (e: React.FormEvent) => {
    e.preventDefault();
    if (!noteContent.trim()) return;

    const newAct: Activity = {
      id: `act-${Date.now()}`,
      type: noteType,
      author: 'Sarah Jenkins',
      target: customer.name,
      title: `Sarah Jenkins logged a ${noteType} with ${customer.name}`,
      content: noteContent,
      time: 'Just now',
    };

    onAddActivity(newAct);
    setNoteContent('');
  };

  return (
    <div className="p-4 md:p-6 max-w-7xl mx-auto space-y-6">
      {/* Top Header Card */}
      <div className="bg-surface-container-lowest p-6 rounded-2xl border border-border-subtle shadow-xs">
        <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-6">
          {/* Logo & Info */}
          <div className="flex items-start sm:items-center gap-4">
            <div className={`w-14 h-14 rounded-2xl flex items-center justify-center font-extrabold text-xl border shadow-xs ${customer.logoBg}`}>
              {customer.logoText}
            </div>
            <div>
              <div className="flex items-center gap-3 flex-wrap">
                <h1 className="text-2xl font-extrabold text-on-surface">{customer.name}</h1>
                <span className="px-2.5 py-0.5 rounded-full text-xs font-bold bg-emerald-100 text-emerald-800">
                  {customer.status}
                </span>
                <span className="text-xs bg-surface-container-high px-2.5 py-0.5 rounded-full font-medium text-on-surface-variant">
                  {customer.industry}
                </span>
              </div>
              <p className="text-xs text-on-surface-variant mt-1 flex items-center gap-2">
                <span>{customer.domain}</span>
                <span>•</span>
                <span>{customer.location}</span>
                <span>•</span>
                <span>Account Owner: <strong className="text-on-surface">{customer.accountOwner.name}</strong></span>
              </p>
            </div>
          </div>

          {/* Quick Actions */}
          <div className="flex items-center gap-2.5 flex-wrap">
            <button
              onClick={() => onNavigate('customers')}
              className="px-3.5 py-2 rounded-lg border border-border-subtle text-xs font-semibold text-on-surface hover:bg-surface-container-low transition-colors"
            >
              ← Back to List
            </button>
            <button
              onClick={() => {
                setNoteType('call');
                window.scrollTo({ top: 400, behavior: 'smooth' });
              }}
              className="px-3.5 py-2 rounded-lg bg-surface-container-low hover:bg-surface-container border border-border-subtle text-xs font-semibold text-on-surface flex items-center gap-1.5 transition-colors"
            >
              <span className="material-symbols-outlined text-[16px]">call</span>
              <span>Log Call</span>
            </button>
            <button
              onClick={() => {
                setNoteType('email');
                window.scrollTo({ top: 400, behavior: 'smooth' });
              }}
              className="px-3.5 py-2 rounded-lg bg-surface-container-low hover:bg-surface-container border border-border-subtle text-xs font-semibold text-on-surface flex items-center gap-1.5 transition-colors"
            >
              <span className="material-symbols-outlined text-[16px]">mail</span>
              <span>Send Email</span>
            </button>
            <button
              onClick={onOpenTaskModal}
              className="px-4 py-2 rounded-lg bg-primary hover:bg-primary/90 text-on-primary text-xs font-semibold shadow-xs flex items-center gap-1.5 transition-all"
            >
              <span className="material-symbols-outlined text-[16px]">add_task</span>
              <span>Add Task</span>
            </button>
          </div>
        </div>
      </div>

      {/* Bento Grid Layout */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Left Column: Details & Contact (3 cols) */}
        <div className="lg:col-span-3 space-y-6">
          {/* Main Contact Card */}
          <div className="bg-surface-container-lowest p-5 rounded-2xl border border-border-subtle shadow-xs">
            <h3 className="text-xs font-bold uppercase tracking-wider text-on-surface-variant mb-4">
              Primary Contact
            </h3>
            <div className="flex items-center gap-3 mb-4">
              <img
                src={customer.mainContact.avatar}
                alt={customer.mainContact.name}
                className="w-12 h-12 rounded-full object-cover ring-2 ring-primary/20"
              />
              <div>
                <p className="text-sm font-bold text-on-surface">{customer.mainContact.name}</p>
                <p className="text-xs text-on-surface-variant">VP of Engineering</p>
              </div>
            </div>

            <div className="space-y-2.5 text-xs">
              <div className="flex items-center gap-2 text-on-surface-variant">
                <span className="material-symbols-outlined text-[16px]">mail</span>
                <a href={`mailto:${customer.mainContact.email}`} className="text-primary hover:underline truncate">
                  {customer.mainContact.email}
                </a>
              </div>
              <div className="flex items-center gap-2 text-on-surface-variant">
                <span className="material-symbols-outlined text-[16px]">call</span>
                <span>{customer.mainContact.phone}</span>
              </div>
            </div>
          </div>

          {/* Account Key Metrics */}
          <div className="bg-surface-container-lowest p-5 rounded-2xl border border-border-subtle shadow-xs space-y-4">
            <h3 className="text-xs font-bold uppercase tracking-wider text-on-surface-variant">
              Account Overview
            </h3>

            <div className="space-y-3 text-xs">
              <div>
                <span className="text-on-surface-variant block mb-0.5">Annual Recurring Revenue</span>
                <span className="text-lg font-extrabold text-on-surface">${customer.arr.toLocaleString()}</span>
              </div>

              <div>
                <span className="text-on-surface-variant block mb-0.5">Contract Renewal Date</span>
                <span className="font-semibold text-on-surface">{customer.renewalDate}</span>
              </div>

              <div>
                <span className="text-on-surface-variant block mb-0.5">Location</span>
                <span className="font-semibold text-on-surface">{customer.location}</span>
              </div>
            </div>

            <div className="pt-2 border-t border-border-subtle">
              <span className="text-xs font-semibold text-on-surface block mb-2">Tags</span>
              <div className="flex flex-wrap gap-1.5">
                {customer.tags.map((tag) => (
                  <span
                    key={tag}
                    className="bg-primary/10 text-primary text-[10px] font-bold px-2 py-0.5 rounded-full"
                  >
                    #{tag}
                  </span>
                ))}
              </div>
            </div>
          </div>
        </div>

        {/* Middle Column: Activity Stream & Notes (6 cols) */}
        <div className="lg:col-span-6 space-y-6">
          {/* Main Tabs Header */}
          <div className="bg-surface-container-lowest p-5 rounded-2xl border border-border-subtle shadow-xs">
            <div className="flex items-center gap-2 border-b border-border-subtle pb-3">
              {[
                { id: 'activity', label: 'Activity Stream', icon: 'timeline' },
                { id: 'contacts', label: 'Contacts (3)', icon: 'contacts' },
                { id: 'deals', label: 'Deals (2)', icon: 'account_tree' },
                { id: 'tickets', label: 'Support Tickets (0)', icon: 'confirmation_number' },
              ].map((tab) => (
                <button
                  key={tab.id}
                  onClick={() => setActiveTab(tab.id as any)}
                  className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
                    activeTab === tab.id
                      ? 'bg-primary text-on-primary shadow-xs'
                      : 'text-on-surface-variant hover:bg-surface-container-low'
                  }`}
                >
                  <span className="material-symbols-outlined text-[16px]">{tab.icon}</span>
                  <span>{tab.label}</span>
                </button>
              ))}
            </div>

            {activeTab === 'activity' && (
              <div className="mt-4 space-y-6">
                {/* Note Log Composer */}
                <form onSubmit={handlePostActivity} className="space-y-3">
                  <div className="flex items-center gap-2">
                    <button
                      type="button"
                      onClick={() => setNoteType('call')}
                      className={`px-3 py-1 rounded-full text-xs font-semibold flex items-center gap-1 transition-colors ${
                        noteType === 'call'
                          ? 'bg-primary/10 text-primary border border-primary/30'
                          : 'bg-surface-container-low text-on-surface-variant'
                      }`}
                    >
                      <span className="material-symbols-outlined text-[14px]">call</span>
                      <span>Call Log</span>
                    </button>
                    <button
                      type="button"
                      onClick={() => setNoteType('email')}
                      className={`px-3 py-1 rounded-full text-xs font-semibold flex items-center gap-1 transition-colors ${
                        noteType === 'email'
                          ? 'bg-primary/10 text-primary border border-primary/30'
                          : 'bg-surface-container-low text-on-surface-variant'
                      }`}
                    >
                      <span className="material-symbols-outlined text-[14px]">mail</span>
                      <span>Email</span>
                    </button>
                    <button
                      type="button"
                      onClick={() => setNoteType('meeting')}
                      className={`px-3 py-1 rounded-full text-xs font-semibold flex items-center gap-1 transition-colors ${
                        noteType === 'meeting'
                          ? 'bg-primary/10 text-primary border border-primary/30'
                          : 'bg-surface-container-low text-on-surface-variant'
                      }`}
                    >
                      <span className="material-symbols-outlined text-[14px]">groups</span>
                      <span>Meeting</span>
                    </button>
                  </div>

                  <textarea
                    rows={3}
                    value={noteContent}
                    onChange={(e) => setNoteContent(e.target.value)}
                    placeholder={`Log a ${noteType} or key meeting takeaways for ${customer.name}...`}
                    className="w-full bg-surface-container-low text-on-surface text-xs p-3 rounded-xl border border-transparent focus:border-primary focus:bg-surface-container-lowest focus:outline-none"
                  />

                  <div className="flex justify-end">
                    <button
                      type="submit"
                      disabled={!noteContent.trim()}
                      className="bg-primary hover:bg-primary/90 disabled:opacity-50 text-on-primary text-xs font-semibold px-4 py-2 rounded-lg shadow-xs transition-all"
                    >
                      Post Note
                    </button>
                  </div>
                </form>

                {/* Timeline List */}
                <div className="space-y-4 pt-2 border-t border-border-subtle">
                  <h4 className="text-xs font-bold text-on-surface">Timeline History</h4>
                  {activities.map((act) => (
                    <div key={act.id} className="flex gap-3 text-xs">
                      <div className="w-8 h-8 rounded-full bg-primary/10 text-primary flex items-center justify-center shrink-0 font-bold">
                        <span className="material-symbols-outlined text-[18px]">
                          {act.type === 'call' ? 'call' : act.type === 'email' ? 'mail' : 'notes'}
                        </span>
                      </div>
                      <div className="flex-1 bg-surface-container-low/40 p-3 rounded-xl border border-border-subtle">
                        <div className="flex items-center justify-between font-bold text-on-surface">
                          <span>{act.title}</span>
                          <span className="text-[10px] text-on-surface-variant font-normal">{act.time}</span>
                        </div>
                        <p className="text-on-surface-variant mt-1">{act.content}</p>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {activeTab === 'contacts' && (
              <div className="mt-4 space-y-3 text-xs">
                <div className="p-3 bg-surface-container-low rounded-xl flex items-center justify-between">
                  <div className="flex items-center gap-2.5">
                    <img src={customer.mainContact.avatar} alt="" className="w-8 h-8 rounded-full" />
                    <div>
                      <p className="font-bold text-on-surface">{customer.mainContact.name}</p>
                      <p className="text-[11px] text-on-surface-variant">VP of Engineering • Primary</p>
                    </div>
                  </div>
                  <span className="text-primary font-semibold">{customer.mainContact.email}</span>
                </div>
              </div>
            )}

            {activeTab === 'deals' && (
              <div className="mt-4 space-y-3 text-xs">
                <div className="p-3 bg-surface-container-low rounded-xl flex items-center justify-between">
                  <div>
                    <p className="font-bold text-on-surface">Acme Corp Redesign</p>
                    <p className="text-[11px] text-on-surface-variant">Stage: Prospect • Priority: Medium</p>
                  </div>
                  <span className="font-bold text-emerald-700 text-sm">$45,000</span>
                </div>
              </div>
            )}
          </div>
        </div>

        {/* Right Column: Health Score & Action Items (3 cols) */}
        <div className="lg:col-span-3 space-y-6">
          {/* Health Gauge Widget */}
          <div className="bg-surface-container-lowest p-5 rounded-2xl border border-border-subtle shadow-xs text-center">
            <h3 className="text-xs font-bold uppercase tracking-wider text-on-surface-variant mb-4">
              Account Health Score
            </h3>

            <div className="relative inline-flex items-center justify-center w-28 h-28 my-2">
              <svg className="w-full h-full transform -rotate-90" viewBox="0 0 36 36">
                <path
                  className="text-surface-container"
                  strokeWidth="3.5"
                  stroke="currentColor"
                  fill="none"
                  d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
                />
                <path
                  className="text-emerald-500"
                  strokeDasharray={`${customer.healthScore}, 100`}
                  strokeWidth="3.5"
                  strokeLinecap="round"
                  stroke="currentColor"
                  fill="none"
                  d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
                />
              </svg>
              <div className="absolute text-center">
                <span className="text-2xl font-extrabold text-on-surface">{customer.healthScore}</span>
                <span className="block text-[9px] text-on-surface-variant uppercase font-bold">/ 100</span>
              </div>
            </div>

            <p className="text-xs font-semibold text-emerald-600 mt-2">{customer.healthTrend}</p>
            <p className="text-[11px] text-on-surface-variant mt-1">High engagement, low ticket volume</p>
          </div>

          {/* Upcoming Account Tasks */}
          <div className="bg-surface-container-lowest p-5 rounded-2xl border border-border-subtle shadow-xs">
            <div className="flex items-center justify-between mb-3">
              <h3 className="text-xs font-bold uppercase tracking-wider text-on-surface-variant">
                Account Tasks ({customerTasks.length})
              </h3>
              <button onClick={onOpenTaskModal} className="text-xs text-primary font-bold hover:underline">
                + Add
              </button>
            </div>

            <div className="space-y-3 text-xs">
              {customerTasks.length === 0 ? (
                <p className="text-on-surface-variant text-[11px]">No pending tasks for this account.</p>
              ) : (
                customerTasks.map((t) => (
                  <div key={t.id} className="p-3 rounded-xl bg-surface-container-low border border-border-subtle">
                    <p className="font-bold text-on-surface">{t.name}</p>
                    <div className="flex items-center justify-between mt-1 text-[11px] text-on-surface-variant">
                      <span>Due: {t.dueDate}</span>
                      <span className="font-semibold text-red-600">{t.priority}</span>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
