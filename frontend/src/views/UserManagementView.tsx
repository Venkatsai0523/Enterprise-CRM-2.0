import React, { useState } from 'react';
import { User } from '../types';

interface UserManagementViewProps {
  users: User[];
  onOpenInviteModal: () => void;
  onUpdateUserStatus: (userId: string, newStatus: User['status']) => void;
}

export const UserManagementView: React.FC<UserManagementViewProps> = ({
  users,
  onOpenInviteModal,
  onUpdateUserStatus,
}) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [roleFilter, setRoleFilter] = useState('All');
  const [teamFilter, setTeamFilter] = useState('All');
  const [statusFilter, setStatusFilter] = useState('All');

  const filteredUsers = users.filter((u) => {
    const matchesSearch =
      u.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      u.email.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesRole = roleFilter === 'All' || u.role === roleFilter;
    const matchesTeam = teamFilter === 'All' || u.team === teamFilter;
    const matchesStatus = statusFilter === 'All' || u.status === statusFilter;

    return matchesSearch && matchesRole && matchesTeam && matchesStatus;
  });

  return (
    <div className="p-4 md:p-6 max-w-7xl mx-auto space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold text-on-surface">Organization & User Management</h1>
          <p className="text-xs text-on-surface-variant mt-0.5">
            Manage team members, roles, permissions, and organization security settings ({users.length} Total Users)
          </p>
        </div>
        <button
          onClick={onOpenInviteModal}
          className="bg-primary hover:bg-primary/90 text-on-primary text-xs font-semibold px-4 py-2 rounded-lg shadow-sm flex items-center gap-1.5 transition-all"
        >
          <span className="material-symbols-outlined text-[16px]">person_add</span>
          <span>Invite User</span>
        </button>
      </div>

      {/* Filter Bar */}
      <div className="bg-surface-container-lowest p-4 rounded-2xl border border-border-subtle shadow-xs flex flex-wrap items-center justify-between gap-3">
        <div className="relative flex-1 min-w-[220px]">
          <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-on-surface-variant text-[18px]">
            search
          </span>
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            placeholder="Search by name or email address..."
            className="w-full bg-surface-container-low text-on-surface text-xs pl-9 pr-4 py-2 rounded-lg border border-transparent focus:border-primary focus:bg-surface-container-lowest focus:outline-none"
          />
        </div>

        <div className="flex items-center gap-2 flex-wrap">
          <select
            value={roleFilter}
            onChange={(e) => setRoleFilter(e.target.value)}
            className="bg-surface-container-low text-on-surface text-xs px-3 py-2 rounded-lg border border-transparent focus:border-primary focus:outline-none font-medium"
          >
            <option value="All">All Roles</option>
            <option value="Admin">Admin</option>
            <option value="Manager">Manager</option>
            <option value="Sales Rep">Sales Rep</option>
            <option value="Support">Support</option>
          </select>

          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="bg-surface-container-low text-on-surface text-xs px-3 py-2 rounded-lg border border-transparent focus:border-primary focus:outline-none font-medium"
          >
            <option value="All">All Statuses</option>
            <option value="Active">Active</option>
            <option value="Pending">Pending</option>
            <option value="Deactivated">Deactivated</option>
          </select>
        </div>
      </div>

      {/* Users Data Table */}
      <div className="bg-surface-container-lowest rounded-2xl border border-border-subtle shadow-xs overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse text-xs">
            <thead>
              <tr className="bg-surface-container-low/60 border-b border-border-subtle text-[11px] font-bold text-on-surface-variant uppercase tracking-wider">
                <th className="p-3.5 pl-5">User</th>
                <th className="p-3.5">Role</th>
                <th className="p-3.5">Team</th>
                <th className="p-3.5">Status</th>
                <th className="p-3.5">Last Active</th>
                <th className="p-3.5 pr-5 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border-subtle">
              {filteredUsers.length === 0 ? (
                <tr>
                  <td colSpan={6} className="p-8 text-center text-on-surface-variant">
                    No users match the selected filters.
                  </td>
                </tr>
              ) : (
                filteredUsers.map((user) => (
                  <tr key={user.id} className="hover:bg-surface-container-low/50 transition-colors">
                    <td className="p-3.5 pl-5">
                      <div className="flex items-center gap-3">
                        {user.avatar ? (
                          <img src={user.avatar} alt="" className="w-9 h-9 rounded-full object-cover" />
                        ) : (
                          <div className="w-9 h-9 rounded-full bg-primary-container text-on-primary-container font-bold flex items-center justify-center text-xs">
                            {user.avatarText || user.name.slice(0, 2).toUpperCase()}
                          </div>
                        )}
                        <div>
                          <p className="font-bold text-on-surface">{user.name}</p>
                          <p className="text-[11px] text-on-surface-variant">{user.email}</p>
                        </div>
                      </div>
                    </td>
                    <td className="p-3.5">
                      <span className="font-semibold bg-surface-container-high text-on-surface px-2.5 py-1 rounded-md text-[11px]">
                        {user.role}
                      </span>
                    </td>
                    <td className="p-3.5 font-medium text-on-surface">{user.team}</td>
                    <td className="p-3.5">
                      <span
                        className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[11px] font-bold ${
                          user.status === 'Active'
                            ? 'bg-emerald-100 text-emerald-800'
                            : user.status === 'Pending'
                            ? 'bg-amber-100 text-amber-800'
                            : 'bg-slate-100 text-slate-700'
                        }`}
                      >
                        <span className="w-1.5 h-1.5 rounded-full bg-current" />
                        {user.status}
                      </span>
                    </td>
                    <td className="p-3.5 text-on-surface-variant">{user.lastActive}</td>
                    <td className="p-3.5 pr-5 text-right">
                      <select
                        value={user.status}
                        onChange={(e) => onUpdateUserStatus(user.id, e.target.value as any)}
                        className="bg-surface-container-low text-xs text-on-surface px-2 py-1 rounded border border-border-subtle focus:outline-none cursor-pointer font-medium"
                      >
                        <option value="Active">Active</option>
                        <option value="Pending">Pending</option>
                        <option value="Deactivated">Deactivated</option>
                      </select>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
