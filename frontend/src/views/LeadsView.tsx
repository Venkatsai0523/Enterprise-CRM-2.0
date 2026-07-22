import React, { useState } from 'react';
import { Lead } from '../types';

interface LeadsViewProps {
  leads: Lead[];
  onMoveLeadStage: (leadId: string, newStatus: Lead['status']) => void;
  onOpenNewLeadModal: () => void;
}

const LEAD_STAGES: Lead['status'][] = ['New Leads', 'Contacted', 'Qualified', 'Nurturing'];

export const LeadsView: React.FC<LeadsViewProps> = ({
  leads,
  onMoveLeadStage,
  onOpenNewLeadModal,
}) => {
  const [searchTerm, setSearchTerm] = useState('');

  const filteredLeads = leads.filter(
    (l) =>
      l.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
      l.contactName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      l.owner.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="p-4 md:p-6 max-w-7xl mx-auto space-y-6">
      {/* Header Bar */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold text-on-surface">Lead Management</h1>
          <p className="text-xs text-on-surface-variant mt-0.5">
            Capture, score, and nurture inbound prospects ({filteredLeads.length} Total Leads)
          </p>
        </div>
        <button
          onClick={onOpenNewLeadModal}
          className="bg-primary hover:bg-primary/90 text-on-primary text-xs font-semibold px-4 py-2 rounded-lg shadow-sm flex items-center gap-1.5 transition-all"
        >
          <span className="material-symbols-outlined text-[16px]">person_add</span>
          <span>Create Lead</span>
        </button>
      </div>

      {/* Filter Bar */}
      <div className="bg-surface-container-lowest p-4 rounded-2xl border border-border-subtle shadow-xs flex items-center justify-between gap-3">
        <div className="relative flex-1">
          <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-on-surface-variant text-[18px]">
            search
          </span>
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            placeholder="Search leads by title or contact person..."
            className="w-full bg-surface-container-low text-on-surface text-xs pl-9 pr-4 py-2 rounded-lg border border-transparent focus:border-primary focus:bg-surface-container-lowest focus:outline-none"
          />
        </div>
      </div>

      {/* Lead Kanban Board */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {LEAD_STAGES.map((status) => {
          const stageLeads = filteredLeads.filter((l) => l.status === status);

          return (
            <div
              key={status}
              className="bg-surface-container-low/50 rounded-2xl p-3.5 border border-border-subtle flex flex-col min-h-[480px]"
            >
              {/* Column Header */}
              <div className="flex items-center justify-between pb-3 mb-3 border-b border-border-subtle">
                <div className="flex items-center gap-2">
                  <h3 className="font-bold text-xs text-on-surface">{status}</h3>
                  <span className="bg-surface-container-high text-on-surface-variant text-[10px] font-bold px-2 py-0.5 rounded-full">
                    {stageLeads.length}
                  </span>
                </div>
              </div>

              {/* Cards List */}
              <div className="space-y-3 flex-1">
                {stageLeads.length === 0 ? (
                  <div className="p-4 rounded-xl border border-dashed border-border-subtle text-center text-[11px] text-on-surface-variant">
                    No leads in {status}
                  </div>
                ) : (
                  stageLeads.map((lead) => (
                    <div
                      key={lead.id}
                      className="bg-surface-container-lowest p-3.5 rounded-xl border border-border-subtle hover:border-primary/50 shadow-xs transition-all space-y-2.5"
                    >
                      <div className="flex items-start justify-between gap-2">
                        <div className="flex items-center gap-1.5">
                          {lead.isHot && (
                            <span
                              className="material-symbols-outlined text-amber-500 text-[18px] filled"
                              title="Hot Lead"
                            >
                              local_fire_department
                            </span>
                          )}
                          <h4 className="font-bold text-xs text-on-surface leading-snug">
                            {lead.title}
                          </h4>
                        </div>
                        <span
                          className={`text-[10px] font-extrabold px-2 py-0.5 rounded-full ${
                            lead.score >= 80
                              ? 'bg-emerald-100 text-emerald-800'
                              : 'bg-blue-100 text-blue-800'
                          }`}
                        >
                          {lead.score} pts
                        </span>
                      </div>

                      <div className="flex items-center justify-between text-xs text-on-surface-variant">
                        <span>Contact: <strong className="text-on-surface">{lead.contactName}</strong></span>
                        <span className="text-[10px]">{lead.date}</span>
                      </div>

                      <div className="flex items-center justify-between pt-2 border-t border-border-subtle text-[11px]">
                        <span className="text-on-surface-variant">Owner: {lead.owner}</span>
                        <select
                          value={lead.status}
                          onChange={(e) => onMoveLeadStage(lead.id, e.target.value as any)}
                          className="bg-surface-container-low text-primary font-bold text-[10px] px-1.5 py-0.5 rounded focus:outline-none cursor-pointer"
                        >
                          {LEAD_STAGES.map((s) => (
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
