import React, { useState } from 'react';
import { Deal } from '../types';

interface PipelineViewProps {
  deals: Deal[];
  onMoveDealStage: (dealId: string, newStage: Deal['stage']) => void;
  onOpenNewDealModal: () => void;
}

const STAGES: Deal['stage'][] = [
  'Prospect',
  'Qualified',
  'Proposal',
  'Negotiation',
  'Closed Won',
];

export const PipelineView: React.FC<PipelineViewProps> = ({
  deals,
  onMoveDealStage,
  onOpenNewDealModal,
}) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [priorityFilter, setPriorityFilter] = useState<string>('All');

  const filteredDeals = deals.filter((d) => {
    const matchesSearch =
      d.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
      d.company.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesPriority = priorityFilter === 'All' || d.priority === priorityFilter;
    return matchesSearch && matchesPriority;
  });

  const totalValue = filteredDeals.reduce((sum, d) => sum + d.amount, 0);

  return (
    <div className="p-4 md:p-6 max-w-7xl mx-auto space-y-6">
      {/* Header and Value Banner */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold text-on-surface">Sales Pipeline Kanban</h1>
          <p className="text-xs text-on-surface-variant mt-0.5">
            Track active opportunities across stages ({filteredDeals.length} Active Deals • Total: ${totalValue.toLocaleString()})
          </p>
        </div>
        <button
          onClick={onOpenNewDealModal}
          className="bg-primary hover:bg-primary/90 text-on-primary text-xs font-semibold px-4 py-2 rounded-lg shadow-sm flex items-center gap-1.5 transition-all"
        >
          <span className="material-symbols-outlined text-[16px]">add</span>
          <span>New Opportunity</span>
        </button>
      </div>

      {/* Filter Bar */}
      <div className="bg-surface-container-lowest p-4 rounded-2xl border border-border-subtle shadow-xs flex flex-wrap items-center justify-between gap-3">
        <div className="relative flex-1 min-w-[200px]">
          <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-on-surface-variant text-[18px]">
            search
          </span>
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            placeholder="Search deals or company names..."
            className="w-full bg-surface-container-low text-on-surface text-xs pl-9 pr-4 py-2 rounded-lg border border-transparent focus:border-primary focus:bg-surface-container-lowest focus:outline-none"
          />
        </div>

        <div className="flex items-center gap-2">
          <select
            value={priorityFilter}
            onChange={(e) => setPriorityFilter(e.target.value)}
            className="bg-surface-container-low text-on-surface text-xs px-3 py-2 rounded-lg border border-transparent focus:border-primary focus:outline-none font-medium"
          >
            <option value="All">All Priorities</option>
            <option value="High">High Priority</option>
            <option value="Medium">Medium Priority</option>
            <option value="Low">Low Priority</option>
          </select>
        </div>
      </div>

      {/* Kanban Board Columns Grid */}
      <div className="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-5 gap-4 overflow-x-auto pb-4">
        {STAGES.map((stage) => {
          const stageDeals = filteredDeals.filter((d) => d.stage === stage);
          const stageTotal = stageDeals.reduce((sum, d) => sum + d.amount, 0);

          return (
            <div
              key={stage}
              className="bg-surface-container-low/50 rounded-2xl p-3 border border-border-subtle flex flex-col min-w-[220px] min-h-[500px]"
            >
              {/* Stage Header */}
              <div className="flex items-center justify-between pb-3 mb-3 border-b border-border-subtle">
                <div className="flex items-center gap-2">
                  <h3 className="font-bold text-xs text-on-surface">{stage}</h3>
                  <span className="bg-surface-container-high text-on-surface-variant text-[10px] font-bold px-2 py-0.5 rounded-full">
                    {stageDeals.length}
                  </span>
                </div>
                <span className="text-[11px] font-extrabold text-primary">
                  ${(stageTotal / 1000).toFixed(0)}k
                </span>
              </div>

              {/* Deal Cards Container */}
              <div className="space-y-3 flex-1">
                {stageDeals.length === 0 ? (
                  <div className="p-4 rounded-xl border border-dashed border-border-subtle text-center text-[11px] text-on-surface-variant">
                    No deals in {stage}
                  </div>
                ) : (
                  stageDeals.map((deal) => (
                    <div
                      key={deal.id}
                      className="bg-surface-container-lowest p-3.5 rounded-xl border border-border-subtle hover:border-primary/50 shadow-xs transition-all space-y-2.5 group"
                    >
                      <div className="flex items-start justify-between gap-2">
                        <h4 className="font-bold text-xs text-on-surface group-hover:text-primary transition-colors leading-snug">
                          {deal.title}
                        </h4>
                        <span
                          className={`text-[9px] font-bold px-1.5 py-0.5 rounded-full ${
                            deal.priority === 'High'
                              ? 'bg-red-100 text-red-700'
                              : deal.priority === 'Medium'
                              ? 'bg-amber-100 text-amber-700'
                              : 'bg-slate-100 text-slate-700'
                          }`}
                        >
                          {deal.priority}
                        </span>
                      </div>

                      <p className="text-[11px] font-medium text-on-surface-variant">{deal.company}</p>

                      <div className="flex items-center justify-between pt-1 border-t border-border-subtle text-xs">
                        <span className="font-extrabold text-on-surface">
                          ${deal.amount.toLocaleString()}
                        </span>
                        <div className="flex items-center gap-1.5">
                          <span className="text-[10px] text-on-surface-variant">Due {deal.dueDate}</span>
                          <img
                            src={deal.owner.avatar}
                            alt={deal.owner.name}
                            className="w-5 h-5 rounded-full object-cover"
                            title={deal.owner.name}
                          />
                        </div>
                      </div>

                      {/* Quick Stage Move Dropdown */}
                      <div className="pt-2 flex items-center justify-between text-[10px] border-t border-border-subtle/50">
                        <span className="text-on-surface-variant">Move Stage:</span>
                        <select
                          value={deal.stage}
                          onChange={(e) => onMoveDealStage(deal.id, e.target.value as any)}
                          className="bg-surface-container-low text-primary font-bold text-[10px] px-1.5 py-0.5 rounded border border-transparent focus:outline-none cursor-pointer"
                        >
                          {STAGES.map((s) => (
                            <option key={s} value={s}>
                              {s}
                            </option>
                          ))}
                        </select>
                      </div>
                    </div>
                  ))
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};
