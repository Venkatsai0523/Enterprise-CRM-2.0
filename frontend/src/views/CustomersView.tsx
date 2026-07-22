import React, { useState } from 'react';
import { Customer } from '../types';

interface CustomersViewProps {
  customers: Customer[];
  onSelectCustomer: (customer: Customer) => void;
  onOpenNewCustomerModal: () => void;
}

export const CustomersView: React.FC<CustomersViewProps> = ({
  customers,
  onSelectCustomer,
  onOpenNewCustomerModal,
}) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState<'All' | 'Active' | 'Onboarding' | 'At Risk'>('All');
  const [industryFilter, setIndustryFilter] = useState('All');
  const [selectedIds, setSelectedIds] = useState<string[]>([]);

  const filteredCustomers = customers.filter((c) => {
    const matchesSearch =
      c.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      c.domain.toLowerCase().includes(searchTerm.toLowerCase()) ||
      c.mainContact.name.toLowerCase().includes(searchTerm.toLowerCase());

    const matchesStatus = statusFilter === 'All' || c.status === statusFilter;
    const matchesIndustry = industryFilter === 'All' || c.industry === industryFilter;

    return matchesSearch && matchesStatus && matchesIndustry;
  });

  const toggleSelectAll = () => {
    if (selectedIds.length === filteredCustomers.length) {
      setSelectedIds([]);
    } else {
      setSelectedIds(filteredCustomers.map((c) => c.id));
    }
  };

  const toggleSelect = (id: string, e: React.MouseEvent) => {
    e.stopPropagation();
    if (selectedIds.includes(id)) {
      setSelectedIds(selectedIds.filter((item) => item !== id));
    } else {
      setSelectedIds([...selectedIds, id]);
    }
  };

  const handleExportCSV = () => {
    const csvContent =
      'data:text/csv;charset=utf-8,' +
      ['Name,Domain,Industry,Status,ARR,Contact,HealthScore']
        .concat(
          filteredCustomers.map(
            (c) =>
              `"${c.name}","${c.domain}","${c.industry}","${c.status}",${c.arr},"${c.mainContact.name}",${c.healthScore}`
          )
        )
        .join('\n');
    const encodedUri = encodeURI(csvContent);
    const link = document.createElement('a');
    link.setAttribute('href', encodedUri);
    link.setAttribute('download', 'nexuscrm_customers.csv');
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  return (
    <div className="p-4 md:p-6 max-w-7xl mx-auto space-y-6">
      {/* Header and Actions */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold text-on-surface">Customers Directory</h1>
          <p className="text-xs text-on-surface-variant mt-0.5">
            Manage client accounts, contracts, renewals, and health scores ({filteredCustomers.length} Total Accounts)
          </p>
        </div>
        <div className="flex items-center gap-2.5">
          <button
            onClick={handleExportCSV}
            className="bg-surface-container-lowest hover:bg-surface-container border border-border-subtle text-on-surface text-xs font-semibold px-3.5 py-2 rounded-lg flex items-center gap-1.5 shadow-xs transition-colors"
          >
            <span className="material-symbols-outlined text-[16px]">download</span>
            <span>Export CSV</span>
          </button>
          <button
            onClick={onOpenNewCustomerModal}
            className="bg-primary hover:bg-primary/90 text-on-primary text-xs font-semibold px-4 py-2 rounded-lg shadow-sm flex items-center gap-1.5 transition-all"
          >
            <span className="material-symbols-outlined text-[16px]">add</span>
            <span>Add Customer</span>
          </button>
        </div>
      </div>

      {/* Filter Toolbar */}
      <div className="bg-surface-container-lowest p-4 rounded-2xl border border-border-subtle shadow-xs flex flex-wrap items-center justify-between gap-3">
        {/* Search */}
        <div className="relative flex-1 min-w-[220px]">
          <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-on-surface-variant text-[18px]">
            search
          </span>
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            placeholder="Search by company, contact name or domain..."
            className="w-full bg-surface-container-low text-on-surface text-xs pl-9 pr-4 py-2 rounded-lg border border-transparent focus:border-primary focus:bg-surface-container-lowest focus:outline-none"
          />
        </div>

        {/* Filter Selects */}
        <div className="flex items-center gap-2 flex-wrap">
          {/* Status filter */}
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value as any)}
            className="bg-surface-container-low text-on-surface text-xs px-3 py-2 rounded-lg border border-transparent focus:border-primary focus:outline-none font-medium"
          >
            <option value="All">All Statuses</option>
            <option value="Active">Active</option>
            <option value="Onboarding">Onboarding</option>
            <option value="At Risk">At Risk</option>
          </select>

          {/* Industry Filter */}
          <select
            value={industryFilter}
            onChange={(e) => setIndustryFilter(e.target.value)}
            className="bg-surface-container-low text-on-surface text-xs px-3 py-2 rounded-lg border border-transparent focus:border-primary focus:outline-none font-medium"
          >
            <option value="All">All Industries</option>
            <option value="Enterprise Software">Enterprise Software</option>
            <option value="Manufacturing">Manufacturing</option>
            <option value="Defense">Defense</option>
            <option value="Finance">Finance</option>
          </select>
        </div>
      </div>

      {/* Customers Data Table */}
      <div className="bg-surface-container-lowest rounded-2xl border border-border-subtle shadow-xs overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-surface-container-low/60 border-b border-border-subtle text-[11px] font-bold text-on-surface-variant uppercase tracking-wider">
                <th className="p-3.5 pl-4 w-10">
                  <input
                    type="checkbox"
                    checked={
                      selectedIds.length > 0 && selectedIds.length === filteredCustomers.length
                    }
                    onChange={toggleSelectAll}
                    className="rounded text-primary focus:ring-primary h-4 w-4 cursor-pointer"
                  />
                </th>
                <th className="p-3.5">Company / Account</th>
                <th className="p-3.5">Industry</th>
                <th className="p-3.5">Status</th>
                <th className="p-3.5">Primary Contact</th>
                <th className="p-3.5">ARR Value</th>
                <th className="p-3.5">Health</th>
                <th className="p-3.5">Account Owner</th>
                <th className="p-3.5 pr-4 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border-subtle text-xs">
              {filteredCustomers.length === 0 ? (
                <tr>
                  <td colSpan={9} className="p-8 text-center text-on-surface-variant">
                    No matching customer accounts found.
                  </td>
                </tr>
              ) : (
                filteredCustomers.map((cust) => (
                  <tr
                    key={cust.id}
                    onClick={() => onSelectCustomer(cust)}
                    className="hover:bg-surface-container-low/80 cursor-pointer transition-colors group"
                  >
                    <td className="p-3.5 pl-4" onClick={(e) => e.stopPropagation()}>
                      <input
                        type="checkbox"
                        checked={selectedIds.includes(cust.id)}
                        onChange={(e) => toggleSelect(cust.id, e as any)}
                        className="rounded text-primary focus:ring-primary h-4 w-4 cursor-pointer"
                      />
                    </td>
                    <td className="p-3.5">
                      <div className="flex items-center gap-3">
                        <div
                          className={`w-9 h-9 rounded-xl flex items-center justify-center font-bold text-xs border ${cust.logoBg}`}
                        >
                          {cust.logoText}
                        </div>
                        <div>
                          <div className="font-bold text-on-surface group-hover:text-primary transition-colors flex items-center gap-1.5">
                            <span>{cust.name}</span>
                          </div>
                          <div className="text-[11px] text-on-surface-variant">{cust.domain}</div>
                        </div>
                      </div>
                    </td>
                    <td className="p-3.5 font-medium text-on-surface">{cust.industry}</td>
                    <td className="p-3.5">
                      <span
                        className={`inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-[11px] font-bold ${
                          cust.status === 'Active'
                            ? 'bg-emerald-100 text-emerald-800'
                            : cust.status === 'Onboarding'
                            ? 'bg-blue-100 text-blue-800'
                            : 'bg-red-100 text-red-800'
                        }`}
                      >
                        <span className="w-1.5 h-1.5 rounded-full bg-current" />
                        {cust.status}
                      </span>
                    </td>
                    <td className="p-3.5">
                      <div className="flex items-center gap-2">
                        <img
                          src={cust.mainContact.avatar}
                          alt={cust.mainContact.name}
                          className="w-6 h-6 rounded-full object-cover"
                        />
                        <div>
                          <p className="font-medium text-on-surface">{cust.mainContact.name}</p>
                          <p className="text-[10px] text-on-surface-variant">{cust.mainContact.email}</p>
                        </div>
                      </div>
                    </td>
                    <td className="p-3.5 font-bold text-on-surface">
                      ${cust.arr.toLocaleString()} /yr
                    </td>
                    <td className="p-3.5">
                      <div className="flex items-center gap-2">
                        <span
                          className={`font-bold ${
                            cust.healthScore >= 80
                              ? 'text-emerald-600'
                              : cust.healthScore >= 60
                              ? 'text-amber-600'
                              : 'text-red-600'
                          }`}
                        >
                          {cust.healthScore}%
                        </span>
                        <div className="w-16 bg-surface-container h-1.5 rounded-full overflow-hidden">
                          <div
                            className={`h-full ${
                              cust.healthScore >= 80
                                ? 'bg-emerald-500'
                                : cust.healthScore >= 60
                                ? 'bg-amber-500'
                                : 'bg-red-500'
                            }`}
                            style={{ width: `${cust.healthScore}%` }}
                          />
                        </div>
                      </div>
                    </td>
                    <td className="p-3.5 font-medium text-on-surface">
                      <div className="flex items-center gap-1.5">
                        <div className="w-5 h-5 rounded-full bg-primary-container text-on-primary-container text-[10px] font-bold flex items-center justify-center">
                          {cust.accountOwner.avatarText}
                        </div>
                        <span>{cust.accountOwner.name}</span>
                      </div>
                    </td>
                    <td className="p-3.5 pr-4 text-right" onClick={(e) => e.stopPropagation()}>
                      <button
                        onClick={() => onSelectCustomer(cust)}
                        className="text-primary hover:bg-primary/10 p-1.5 rounded-lg transition-colors"
                        title="View Profile"
                      >
                        <span className="material-symbols-outlined text-[18px]">chevron_right</span>
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination Footer */}
        <div className="p-3.5 border-t border-border-subtle bg-surface-container-low/30 flex items-center justify-between text-xs text-on-surface-variant">
          <span>Showing 1 to {filteredCustomers.length} of {customers.length} entries</span>
          <div className="flex items-center gap-2">
            <button className="px-3 py-1 rounded border border-border-subtle hover:bg-surface-container-low disabled:opacity-50">
              Previous
            </button>
            <button className="px-3 py-1 rounded bg-primary text-on-primary font-bold">1</button>
            <button className="px-3 py-1 rounded border border-border-subtle hover:bg-surface-container-low disabled:opacity-50">
              Next
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
