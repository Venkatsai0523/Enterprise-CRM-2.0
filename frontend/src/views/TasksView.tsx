import React, { useState } from 'react';
import { Task } from '../types';

interface TasksViewProps {
  tasks: Task[];
  onToggleTask: (taskId: string) => void;
  onOpenNewTaskModal: () => void;
}

export const TasksView: React.FC<TasksViewProps> = ({
  tasks,
  onToggleTask,
  onOpenNewTaskModal,
}) => {
  const [filter, setFilter] = useState<'All' | 'High' | 'Completed'>('All');
  const [selectedDate, setSelectedDate] = useState<number>(24);

  const filteredTasks = tasks.filter((t) => {
    if (filter === 'High') return t.priority === 'High' && !t.completed;
    if (filter === 'Completed') return t.completed;
    return true;
  });

  const highPriorityTasks = tasks.filter((t) => t.isHighPriorityCard);

  return (
    <div className="p-4 md:p-6 max-w-7xl mx-auto space-y-6">
      {/* Header Bar */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold text-on-surface">Tasks & Daily Agenda</h1>
          <p className="text-xs text-on-surface-variant mt-0.5">
            Manage action items, follow-ups, and calendar deadlines
          </p>
        </div>
        <button
          onClick={onOpenNewTaskModal}
          className="bg-primary hover:bg-primary/90 text-on-primary text-xs font-semibold px-4 py-2 rounded-lg shadow-sm flex items-center gap-1.5 transition-all"
        >
          <span className="material-symbols-outlined text-[16px]">add_task</span>
          <span>New Task</span>
        </button>
      </div>

      {/* High Priority Focus Section */}
      {highPriorityTasks.length > 0 && (
        <div className="space-y-3">
          <h2 className="text-xs font-bold uppercase tracking-wider text-red-600 flex items-center gap-1.5">
            <span className="material-symbols-outlined text-[16px]">priority_high</span>
            <span>High Priority & Overdue Action Items</span>
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {highPriorityTasks.map((t) => (
              <div
                key={t.id}
                className="bg-surface-container-lowest p-4 rounded-2xl border-2 border-red-200 shadow-xs flex items-start justify-between gap-4"
              >
                <div className="space-y-1.5 flex-1">
                  <div className="flex items-center gap-2">
                    <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-red-100 text-red-700">
                      {t.dueStatus || 'Urgent'}
                    </span>
                    <span className="text-xs font-medium text-on-surface-variant">{t.deal}</span>
                  </div>
                  <h3 className="font-bold text-sm text-on-surface">{t.name}</h3>
                  <div className="flex items-center gap-2 text-xs text-on-surface-variant pt-1">
                    <img src={t.assignee.avatar} alt="" className="w-5 h-5 rounded-full object-cover" />
                    <span>Assignee: {t.assignee.name}</span>
                  </div>
                </div>

                <button
                  onClick={() => onToggleTask(t.id)}
                  className={`px-3 py-1.5 rounded-lg text-xs font-bold transition-colors ${
                    t.completed
                      ? 'bg-emerald-100 text-emerald-800'
                      : 'bg-primary text-on-primary hover:bg-primary/90'
                  }`}
                >
                  {t.completed ? 'Done ✓' : 'Mark Done'}
                </button>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Main Grid: Task List + Calendar Agenda */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Left 8 Cols: Task Data Table */}
        <div className="lg:col-span-8 space-y-4">
          <div className="bg-surface-container-lowest p-4 rounded-2xl border border-border-subtle shadow-xs flex items-center justify-between gap-3">
            <div className="flex items-center gap-1.5">
              {(['All', 'High', 'Completed'] as const).map((tab) => (
                <button
                  key={tab}
                  onClick={() => setFilter(tab)}
                  className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
                    filter === tab
                      ? 'bg-primary text-on-primary shadow-xs'
                      : 'text-on-surface-variant hover:bg-surface-container-low'
                  }`}
                >
                  {tab}
                </button>
              ))}
            </div>
            <span className="text-xs text-on-surface-variant font-medium">
              Showing {filteredTasks.length} tasks
            </span>
          </div>

          <div className="bg-surface-container-lowest rounded-2xl border border-border-subtle shadow-xs overflow-hidden">
            <div className="divide-y divide-border-subtle">
              {filteredTasks.map((task) => (
                <div
                  key={task.id}
                  className={`p-4 flex items-center justify-between gap-4 hover:bg-surface-container-low/50 transition-colors ${
                    task.completed ? 'opacity-60 bg-surface-container-low/20' : ''
                  }`}
                >
                  <div className="flex items-center gap-3 min-w-0 flex-1">
                    <input
                      type="checkbox"
                      checked={task.completed}
                      onChange={() => onToggleTask(task.id)}
                      className="rounded text-primary focus:ring-primary h-5 w-5 cursor-pointer"
                    />
                    <div className="min-w-0 flex-1">
                      <p
                        className={`font-bold text-xs text-on-surface truncate ${
                          task.completed ? 'line-through text-on-surface-variant' : ''
                        }`}
                      >
                        {task.name}
                      </p>
                      <p className="text-[11px] text-on-surface-variant truncate">{task.deal}</p>
                    </div>
                  </div>

                  <div className="flex items-center gap-4 text-xs shrink-0">
                    <span
                      className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                        task.priority === 'High'
                          ? 'bg-red-100 text-red-700'
                          : 'bg-amber-100 text-amber-700'
                      }`}
                    >
                      {task.priority}
                    </span>

                    <span className="text-on-surface-variant text-[11px] font-medium hidden sm:inline">
                      {task.dueDate}
                    </span>

                    <img
                      src={task.assignee.avatar}
                      alt={task.assignee.name}
                      className="w-6 h-6 rounded-full object-cover"
                      title={task.assignee.name}
                    />
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Right 4 Cols: Calendar & Agenda Widget */}
        <div className="lg:col-span-4 space-y-6">
          {/* Calendar Picker Widget */}
          <div className="bg-surface-container-lowest p-5 rounded-2xl border border-border-subtle shadow-xs">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-xs font-bold uppercase tracking-wider text-on-surface">October 2024</h3>
              <div className="flex items-center gap-1">
                <button className="p-1 text-on-surface-variant hover:text-on-surface rounded">
                  <span className="material-symbols-outlined text-[18px]">chevron_left</span>
                </button>
                <button className="p-1 text-on-surface-variant hover:text-on-surface rounded">
                  <span className="material-symbols-outlined text-[18px]">chevron_right</span>
                </button>
              </div>
            </div>

            {/* Days Grid */}
            <div className="grid grid-cols-7 gap-1 text-center text-[10px] font-bold text-on-surface-variant mb-2">
              <span>S</span>
              <span>M</span>
              <span>T</span>
              <span>W</span>
              <span>T</span>
              <span>F</span>
              <span>S</span>
            </div>
            <div className="grid grid-cols-7 gap-1 text-center text-xs">
              {Array.from({ length: 31 }, (_, i) => i + 1).map((day) => (
                <button
                  key={day}
                  onClick={() => setSelectedDate(day)}
                  className={`py-1.5 rounded-lg font-semibold transition-all ${
                    selectedDate === day
                      ? 'bg-primary text-on-primary font-bold shadow-xs'
                      : 'hover:bg-surface-container-low text-on-surface'
                  }`}
                >
                  {day}
                </button>
              ))}
            </div>
          </div>

          {/* Today Agenda Timeline */}
          <div className="bg-surface-container-lowest p-5 rounded-2xl border border-border-subtle shadow-xs space-y-4">
            <h3 className="text-xs font-bold uppercase tracking-wider text-on-surface">
              Agenda for Oct {selectedDate}
            </h3>

            <div className="space-y-3 text-xs">
              <div className="p-3 rounded-xl bg-primary/10 border border-primary/20 flex gap-3">
                <div className="font-bold text-primary text-[11px] shrink-0">10:00 AM</div>
                <div>
                  <p className="font-bold text-on-surface">Acme Corp Strategy Call</p>
                  <p className="text-[11px] text-on-surface-variant">Zoom Meeting with Sarah Jenkins</p>
                </div>
              </div>

              <div className="p-3 rounded-xl bg-surface-container-low border border-border-subtle flex gap-3">
                <div className="font-bold text-on-surface-variant text-[11px] shrink-0">02:30 PM</div>
                <div>
                  <p className="font-bold text-on-surface">Stark Ind. Contract Review</p>
                  <p className="text-[11px] text-on-surface-variant">Internal Sync</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
