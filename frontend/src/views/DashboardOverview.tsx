import React from 'react';
import { Customer, Task, Activity, ViewMode, Deal, Lead } from '../types';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  AreaChart,
  Area,
} from 'recharts';

interface DashboardOverviewProps {
  customers: Customer[];
  tasks: Task[];
  activities: Activity[];
  onNavigate: (view: ViewMode) => void;
  onOpenModal: (modalType: 'customer' | 'lead' | 'deal' | 'task') => void;
  userName?: string;
  deals: Deal[];
  leads: Lead[];
}

export const DashboardOverview: React.FC<DashboardOverviewProps> = ({
  tasks,
  activities,
  onNavigate,
  onOpenModal,
  userName,
  deals = [],
  leads = [],
}) => {
  const totalRevenue = deals.filter(d => d.stage === 'Closed Won').reduce((acc, d) => acc + d.amount, 0);
  const pipelineValue = deals.filter(d => d.stage !== 'Closed Won').reduce((acc, d) => acc + d.amount, 0);
  const newLeadsCount = leads.filter(l => l.status === 'New Leads').length;
  const openOpportunitiesCount = deals.filter(d => d.stage !== 'Closed Won').length;
  const tasksDueCount = tasks.filter(t => !t.completed).length;
  const dealsWonCount = deals.filter(d => d.stage === 'Closed Won').length;

  const REVENUE_DATA = [
    { month: 'Q1', revenue: totalRevenue * 0.4 },
    { month: 'Q2', revenue: totalRevenue * 0.7 },
    { month: 'Q3', revenue: totalRevenue },
  ];

  const FUNNEL_DATA = [
    { stage: 'Prospects', value: deals.filter(d => d.stage === 'Prospect').length, fill: '#004ac6' },
    { stage: 'Qualified', value: deals.filter(d => d.stage === 'Qualified').length, fill: '#2563eb' },
    { stage: 'Proposals', value: deals.filter(d => d.stage === 'Proposal').length, fill: '#6faafd' },
    { stage: 'Negotiations', value: deals.filter(d => d.stage === 'Negotiation').length, fill: '#93c5fd' },
    { stage: 'Won Deals', value: dealsWonCount, fill: '#16A34A' },
  ];
  return (
    <div className="p-4 md:p-6 space-y-6 max-w-7xl mx-auto">
      {/* Top Banner with Quick Actions */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 bg-surface-container-lowest p-5 rounded-2xl border border-border-subtle shadow-xs">
        <div>
          <h1 className="text-xl font-bold text-on-surface">Welcome back, {userName || 'User'} 👋</h1>
          <p className="text-xs text-on-surface-variant mt-0.5">
            Here is your sales performance and organization summary for today.
          </p>
        </div>
        <div className="flex flex-wrap items-center gap-2">
          <button
            onClick={() => onOpenModal('customer')}
            className="bg-primary hover:bg-primary/90 text-on-primary text-xs font-semibold px-3.5 py-2 rounded-lg shadow-xs flex items-center gap-1.5 transition-all"
          >
            <span className="material-symbols-outlined text-[16px]">add</span>
            <span>Customer</span>
          </button>
          <button
            onClick={() => onOpenModal('lead')}
            className="bg-surface-container-low hover:bg-surface-container border border-border-subtle text-on-surface text-xs font-semibold px-3.5 py-2 rounded-lg flex items-center gap-1.5 transition-all"
          >
            <span className="material-symbols-outlined text-[16px]">person_add</span>
            <span>Lead</span>
          </button>
          <button
            onClick={() => onOpenModal('deal')}
            className="bg-surface-container-low hover:bg-surface-container border border-border-subtle text-on-surface text-xs font-semibold px-3.5 py-2 rounded-lg flex items-center gap-1.5 transition-all"
          >
            <span className="material-symbols-outlined text-[16px]">account_tree</span>
            <span>Deal</span>
          </button>
          <button
            onClick={() => onOpenModal('task')}
            className="bg-surface-container-low hover:bg-surface-container border border-border-subtle text-on-surface text-xs font-semibold px-3.5 py-2 rounded-lg flex items-center gap-1.5 transition-all"
          >
            <span className="material-symbols-outlined text-[16px]">add_task</span>
            <span>Task</span>
          </button>
        </div>
      </div>

      {/* KPI Cards Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-4">
        {/* KPI 1 */}
        <div className="bg-surface-container-lowest p-4 rounded-2xl border border-border-subtle shadow-xs">
          <div className="flex items-center justify-between mb-2">
            <span className="text-xs font-semibold text-on-surface-variant">Total Revenue</span>
            <div className="w-8 h-8 rounded-lg bg-emerald-100 text-emerald-700 flex items-center justify-center">
              <span className="material-symbols-outlined text-[18px]">payments</span>
            </div>
          </div>
          <div className="text-xl font-extrabold text-on-surface">${totalRevenue.toLocaleString()}</div>
          <div className="flex items-center gap-1 text-[11px] text-emerald-600 font-semibold mt-1">
            <span className="material-symbols-outlined text-[14px]">trending_up</span>
            <span>Live database metrics</span>
          </div>
        </div>

        {/* KPI 2 */}
        <div className="bg-surface-container-lowest p-4 rounded-2xl border border-border-subtle shadow-xs">
          <div className="flex items-center justify-between mb-2">
            <span className="text-xs font-semibold text-on-surface-variant">New Leads</span>
            <div className="w-8 h-8 rounded-lg bg-blue-100 text-blue-700 flex items-center justify-center">
              <span className="material-symbols-outlined text-[18px]">person_add</span>
            </div>
          </div>
          <div className="text-xl font-extrabold text-on-surface">{newLeadsCount}</div>
          <div className="flex items-center gap-1 text-[11px] text-blue-600 font-semibold mt-1">
            <span className="material-symbols-outlined text-[14px]">trending_up</span>
            <span>Active leads</span>
          </div>
        </div>

        {/* KPI 3 */}
        <div className="bg-surface-container-lowest p-4 rounded-2xl border border-border-subtle shadow-xs">
          <div className="flex items-center justify-between mb-2">
            <span className="text-xs font-semibold text-on-surface-variant">Open Opportunities</span>
            <div className="w-8 h-8 rounded-lg bg-indigo-100 text-indigo-700 flex items-center justify-center">
              <span className="material-symbols-outlined text-[18px]">account_tree</span>
            </div>
          </div>
          <div className="text-xl font-extrabold text-on-surface">{openOpportunitiesCount}</div>
          <div className="text-[11px] text-on-surface-variant mt-1 font-medium font-semibold text-indigo-600">
            ${pipelineValue.toLocaleString()} Pipeline Value
          </div>
        </div>

        {/* KPI 4 */}
        <div className="bg-surface-container-lowest p-4 rounded-2xl border border-border-subtle shadow-xs">
          <div className="flex items-center justify-between mb-2">
            <span className="text-xs font-semibold text-on-surface-variant">Tasks Due Today</span>
            <div className="w-8 h-8 rounded-lg bg-amber-100 text-amber-700 flex items-center justify-center">
              <span className="material-symbols-outlined text-[18px]">task_alt</span>
            </div>
          </div>
          <div className="text-xl font-extrabold text-on-surface">{tasksDueCount}</div>
          <div className="text-[11px] text-amber-700 font-semibold mt-1">
            {tasks.filter(t => t.priority === 'High' && !t.completed).length} High Priority
          </div>
        </div>

        {/* KPI 5 */}
        <div className="bg-surface-container-lowest p-4 rounded-2xl border border-border-subtle shadow-xs">
          <div className="flex items-center justify-between mb-2">
            <span className="text-xs font-semibold text-on-surface-variant">Deals Won</span>
            <div className="w-8 h-8 rounded-lg bg-purple-100 text-purple-700 flex items-center justify-center">
              <span className="material-symbols-outlined text-[18px]">emoji_events</span>
            </div>
          </div>
          <div className="text-xl font-extrabold text-on-surface">{dealsWonCount}</div>
          <div className="text-[11px] text-purple-700 font-semibold mt-1">
            {deals.length > 0 ? ((dealsWonCount / deals.length) * 100).toFixed(0) : '0'}% Win Rate
          </div>
        </div>
      </div>

      {/* Analytics Visual Charts Row */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Revenue Growth Chart */}
        <div className="lg:col-span-2 bg-surface-container-lowest p-5 rounded-2xl border border-border-subtle shadow-xs flex flex-col">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h3 className="text-sm font-bold text-on-surface">Revenue Trend & Target</h3>
              <p className="text-[11px] text-on-surface-variant">Monthly recurring revenue (MRR) performance</p>
            </div>
            <button
              onClick={() => onNavigate('analytics')}
              className="text-xs text-primary font-semibold hover:underline"
            >
              Full Analytics →
            </button>
          </div>
          <div className="h-64 w-full pt-2">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={REVENUE_DATA}>
                <defs>
                  <linearGradient id="colorRev" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#004ac6" stopOpacity={0.8}/>
                    <stop offset="95%" stopColor="#004ac6" stopOpacity={0.0}/>
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#E5E7EB" />
                <XAxis dataKey="month" tickLine={false} axisLine={{ stroke: '#E5E7EB' }} tick={{ fontSize: 11 }} />
                <YAxis tickLine={false} axisLine={false} tick={{ fontSize: 11 }} tickFormatter={(val) => `$${val/1000}k`} />
                <Tooltip formatter={(value: number) => [`$${value.toLocaleString()}`, 'Revenue']} />
                <Area type="monotone" dataKey="revenue" stroke="#004ac6" strokeWidth={3} fillOpacity={1} fill="url(#colorRev)" />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Sales Funnel Chart */}
        <div className="bg-surface-container-lowest p-5 rounded-2xl border border-border-subtle shadow-xs flex flex-col justify-between">
          <div>
            <h3 className="text-sm font-bold text-on-surface">Sales Pipeline Breakdown</h3>
            <p className="text-[11px] text-on-surface-variant mb-4">Stage conversion overview</p>
            <div className="h-52 w-full">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={FUNNEL_DATA} layout="vertical">
                  <XAxis type="number" hide />
                  <YAxis dataKey="stage" type="category" axisLine={false} tickLine={false} tick={{ fontSize: 11 }} width={80} />
                  <Tooltip formatter={(val: number) => [val, 'Count']} />
                  <Bar dataKey="value" radius={[0, 6, 6, 0]} fill="#2563eb" />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>
          <button
            onClick={() => onNavigate('pipeline')}
            className="w-full mt-2 py-2 bg-surface-container-low hover:bg-surface-container text-primary text-xs font-semibold rounded-lg text-center transition-colors"
          >
            Open Pipeline Kanban
          </button>
        </div>
      </div>

      {/* Activity Timeline & Upcoming Tasks Row */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Activity Feed */}
        <div className="lg:col-span-2 bg-surface-container-lowest p-5 rounded-2xl border border-border-subtle shadow-xs">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-sm font-bold text-on-surface">Recent Team Activities</h3>
            <button
              onClick={() => onNavigate('notifications')}
              className="text-xs text-primary font-semibold hover:underline"
            >
              View All Notifications
            </button>
          </div>

          <div className="space-y-4">
            {activities.map((act) => (
              <div key={act.id} className="flex items-start gap-3 p-3 rounded-xl bg-surface-container-low/50 hover:bg-surface-container-low transition-colors">
                <div className="w-8 h-8 rounded-full bg-primary/10 text-primary flex items-center justify-center shrink-0 font-bold text-xs">
                  {act.type === 'call' ? (
                    <span className="material-symbols-outlined text-[18px]">call</span>
                  ) : act.type === 'email' ? (
                    <span className="material-symbols-outlined text-[18px]">mail</span>
                  ) : (
                    <span className="material-symbols-outlined text-[18px]">sync_alt</span>
                  )}
                </div>
                <div className="flex-1 min-w-0">
                  <div className="flex items-center justify-between">
                    <p className="text-xs font-bold text-on-surface truncate">{act.title}</p>
                    <span className="text-[10px] text-on-surface-variant">{act.time}</span>
                  </div>
                  <p className="text-xs text-on-surface-variant mt-0.5 line-clamp-2">{act.content}</p>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Priority Tasks Widget */}
        <div className="bg-surface-container-lowest p-5 rounded-2xl border border-border-subtle shadow-xs flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between mb-3">
              <h3 className="text-sm font-bold text-on-surface">Tasks & Agenda</h3>
              <button
                onClick={() => onNavigate('tasks')}
                className="text-xs text-primary font-semibold hover:underline"
              >
                Go to Tasks →
              </button>
            </div>

            <div className="space-y-3">
              {tasks.slice(0, 3).map((task) => (
                <div
                  key={task.id}
                  className="p-3 rounded-xl border border-border-subtle hover:border-primary/40 transition-all flex flex-col gap-2"
                >
                  <div className="flex items-center justify-between">
                    <span className="text-xs font-bold text-on-surface truncate max-w-[180px]">
                      {task.name}
                    </span>
                    <span
                      className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${
                        task.priority === 'High'
                          ? 'bg-red-100 text-red-700'
                          : 'bg-amber-100 text-amber-700'
                      }`}
                    >
                      {task.priority}
                    </span>
                  </div>

                  <div className="flex items-center justify-between text-[11px] text-on-surface-variant">
                    <span>{task.deal}</span>
                    <span className="font-semibold text-on-surface">{task.dueDate}</span>
                  </div>

                  <div className="w-full bg-surface-container h-1.5 rounded-full overflow-hidden">
                    <div
                      className="bg-primary h-full"
                      style={{ width: `${task.progress}%` }}
                    />
                  </div>
                </div>
              ))}
            </div>
          </div>

          <button
            onClick={() => onOpenModal('task')}
            className="w-full mt-4 py-2 border border-dashed border-primary text-primary hover:bg-primary/5 text-xs font-semibold rounded-xl flex items-center justify-center gap-1.5 transition-colors"
          >
            <span className="material-symbols-outlined text-[16px]">add</span>
            <span>Create New Task</span>
          </button>
        </div>
      </div>
    </div>
  );
};
