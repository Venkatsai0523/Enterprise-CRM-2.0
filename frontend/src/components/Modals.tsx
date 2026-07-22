import React, { useState } from 'react';
import * as z from 'zod';
import { Customer, Lead, Deal, Task, User } from '../types';

interface ModalsProps {
  activeModal: 'customer' | 'lead' | 'deal' | 'task' | 'invite' | null;
  onClose: () => void;
  onAddCustomer: (c: Customer) => void;
  onAddLead: (l: Lead) => void;
  onAddDeal: (d: Deal) => void;
  onAddTask: (t: Task) => void;
  onAddUser: (u: User) => void;
}

const customerSchema = z.object({
  name: z.string().min(2, 'Company name must be at least 2 characters'),
  domain: z.string().min(2, 'Domain name is required'),
});

const leadSchema = z.object({
  title: z.string().min(2, 'Lead opportunity title must be at least 2 characters'),
  contactName: z.string().min(2, 'Contact person name must be at least 2 characters'),
});

const dealSchema = z.object({
  title: z.string().min(2, 'Deal name must be at least 2 characters'),
  amount: z.number().min(1, 'Deal value must be greater than 0'),
});

const taskSchema = z.object({
  name: z.string().min(2, 'Task action item must be at least 2 characters'),
});

const inviteSchema = z.object({
  name: z.string().min(2, 'Full name is required'),
  email: z.string().email('Please enter a valid email address'),
});

export const Modals: React.FC<ModalsProps> = ({
  activeModal,
  onClose,
  onAddCustomer,
  onAddLead,
  onAddDeal,
  onAddTask,
  onAddUser,
}) => {
  // New Customer State
  const [custName, setCustName] = useState('');
  const [custDomain, setCustDomain] = useState('');
  const [custIndustry, setCustIndustry] = useState('Enterprise Software');
  const [custContactName, setCustContactName] = useState('');
  const [custContactEmail, setCustContactEmail] = useState('');
  const [custValue, setCustValue] = useState('');

  // New Lead State
  const [leadTitle, setLeadTitle] = useState('');
  const [leadContact, setLeadContact] = useState('');
  const [leadOwner, setLeadOwner] = useState('');

  // New Deal State
  const [dealTitle, setDealTitle] = useState('');
  const [dealCompany, setDealCompany] = useState('');
  const [dealAmount, setDealAmount] = useState('');
  const [dealPriority, setDealPriority] = useState<'Low' | 'Medium' | 'High'>('High');

  // New Task State
  const [taskName, setTaskName] = useState('');
  const [taskDeal, setTaskDeal] = useState('');
  const [taskPriority, setTaskPriority] = useState<'Low' | 'Medium' | 'High'>('High');

  // New User State
  const [userName, setUserName] = useState('');
  const [userEmail, setUserEmail] = useState('');
  const [userRole, setUserRole] = useState<'Admin' | 'Manager' | 'Sales Rep' | 'Support'>('Sales Rep');

  // Form Validation Errors
  const [formErrors, setFormErrors] = useState<Record<string, string>>({});

  if (!activeModal) return null;

  const handleCustomerSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const result = customerSchema.safeParse({ name: custName, domain: custDomain });
    if (!result.success) {
      const errs: any = {};
      result.error.errors.forEach((err) => {
        errs[err.path[0]] = err.message;
      });
      setFormErrors(errs);
      return;
    }
    setFormErrors({});

    const newCust: Customer = {
      id: `cust-${Date.now()}`,
      name: custName,
      domain: custDomain,
      logoText: custName.slice(0, 2).toUpperCase(),
      logoBg: 'bg-emerald-100 text-emerald-800 border-emerald-200',
      status: 'Active',
      industry: custIndustry,
      mainContact: {
        name: custContactName || 'Main Contact',
        email: custContactEmail || 'contact@domain.com',
        phone: '+1 (555) 019-8800',
        avatar: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150&auto=format&fit=crop&q=80',
      },
      totalValue: Number(custValue) || 50000,
      lastActivity: 'Just now',
      accountOwner: { name: 'Sarah Jenkins', avatarText: 'SJ' },
      arr: Number(custValue) || 50000,
      renewalDate: 'Oct 2025',
      tags: ['New Account'],
      healthScore: 90,
      healthTrend: '+10 this month',
      location: 'San Francisco, CA',
    };
    onAddCustomer(newCust);
    onClose();
  };

  const handleLeadSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const result = leadSchema.safeParse({ title: leadTitle, contactName: leadContact });
    if (!result.success) {
      const errs: any = {};
      result.error.errors.forEach((err) => {
        errs[err.path[0]] = err.message;
      });
      setFormErrors(errs);
      return;
    }
    setFormErrors({});

    const newLead: Lead = {
      id: `lead-${Date.now()}`,
      title: leadTitle,
      contactName: leadContact,
      date: 'Today',
      score: 88,
      status: 'New Leads',
      owner: leadOwner,
      isHot: true,
    };
    onAddLead(newLead);
    onClose();
  };

  const handleDealSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const amountVal = Number(dealAmount);
    const result = dealSchema.safeParse({ title: dealTitle, amount: amountVal });
    if (!result.success) {
      const errs: any = {};
      result.error.errors.forEach((err) => {
        errs[err.path[0]] = err.message;
      });
      setFormErrors(errs);
      return;
    }
    setFormErrors({});

    const newDeal: Deal = {
      id: `deal-${Date.now()}`,
      title: dealTitle,
      company: dealCompany,
      amount: amountVal,
      priority: dealPriority,
      stage: 'Prospect',
      dueDate: 'Nov 15',
      owner: {
        name: 'Sarah Jenkins',
        avatar: 'https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=150&auto=format&fit=crop&q=80',
      },
    };
    onAddDeal(newDeal);
    onClose();
  };

  const handleTaskSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const result = taskSchema.safeParse({ name: taskName });
    if (!result.success) {
      const errs: any = {};
      result.error.errors.forEach((err) => {
        errs[err.path[0]] = err.message;
      });
      setFormErrors(errs);
      return;
    }
    setFormErrors({});

    const newTask: Task = {
      id: `task-${Date.now()}`,
      name: taskName,
      deal: taskDeal,
      priority: taskPriority,
      dueDate: 'Today',
      dueStatus: 'Due Today',
      assignee: {
        name: 'Sarah Jenkins',
        avatar: 'https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=150&auto=format&fit=crop&q=80',
      },
      progress: 0,
      completed: false,
    };
    onAddTask(newTask);
    onClose();
  };

  const handleUserSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const result = inviteSchema.safeParse({ name: userName, email: userEmail });
    if (!result.success) {
      const errs: any = {};
      result.error.errors.forEach((err) => {
        errs[err.path[0]] = err.message;
      });
      setFormErrors(errs);
      return;
    }
    setFormErrors({});

    const newUser: User = {
      id: `usr-${Date.now()}`,
      name: userName,
      email: userEmail,
      role: userRole,
      team: 'Enterprise Sales',
      status: 'Pending',
      lastActive: 'Never',
    };
    onAddUser(newUser);
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 bg-black/50 backdrop-blur-xs flex items-center justify-center p-4">
      <div className="bg-surface-container-lowest w-full max-w-md rounded-2xl border border-border-subtle shadow-2xl p-6 relative animate-in fade-in zoom-in-95 duration-200">
        {/* Close Button */}
        <button
          onClick={onClose}
          className="absolute top-4 right-4 text-on-surface-variant hover:text-on-surface p-1 rounded-lg"
        >
          <span className="material-symbols-outlined text-[20px]">close</span>
        </button>

        {/* CUSTOMER MODAL */}
        {activeModal === 'customer' && (
          <form onSubmit={handleCustomerSubmit} className="space-y-4">
            <h2 className="text-base font-bold text-on-surface flex items-center gap-2">
              <span className="material-symbols-outlined text-primary text-[20px]">groups</span>
              <span>Add New Customer Account</span>
            </h2>
            <div>
              <label className="block text-xs font-semibold text-on-surface mb-1">Company Name</label>
              <input
                type="text"
                value={custName}
                onChange={(e) => setCustName(e.target.value)}
                placeholder="e.g. Nexus Technology Inc"
                className="w-full bg-surface-container-low text-on-surface text-xs p-2.5 rounded-lg border border-transparent focus:border-primary focus:outline-none"
              />
              {formErrors.name && (
                <p className="text-red-500 text-[11px] mt-1 font-semibold">{formErrors.name}</p>
              )}
            </div>
            <div>
              <label className="block text-xs font-semibold text-on-surface mb-1">Domain</label>
              <input
                type="text"
                value={custDomain}
                onChange={(e) => setCustDomain(e.target.value)}
                placeholder="nexustech.io"
                className="w-full bg-surface-container-low text-on-surface text-xs p-2.5 rounded-lg border border-transparent focus:border-primary focus:outline-none"
              />
              {formErrors.domain && (
                <p className="text-red-500 text-[11px] mt-1 font-semibold">{formErrors.domain}</p>
              )}
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-xs font-semibold text-on-surface mb-1">Contact Name</label>
                <input
                  type="text"
                  value={custContactName}
                  onChange={(e) => setCustContactName(e.target.value)}
                  placeholder="John Smith"
                  className="w-full bg-surface-container-low text-on-surface text-xs p-2.5 rounded-lg border border-transparent focus:border-primary focus:outline-none"
                />
              </div>
              <div>
                <label className="block text-xs font-semibold text-on-surface mb-1">Annual Value ($)</label>
                <input
                  type="number"
                  value={custValue}
                  onChange={(e) => setCustValue(e.target.value)}
                  className="w-full bg-surface-container-low text-on-surface text-xs p-2.5 rounded-lg border border-transparent focus:border-primary focus:outline-none"
                />
              </div>
            </div>
            <button
              type="submit"
              className="w-full mt-2 bg-primary hover:bg-primary/90 text-on-primary text-xs font-bold py-2.5 rounded-lg shadow-xs"
            >
              Save Customer Account
            </button>
          </form>
        )}

        {/* LEAD MODAL */}
        {activeModal === 'lead' && (
          <form onSubmit={handleLeadSubmit} className="space-y-4">
            <h2 className="text-base font-bold text-on-surface flex items-center gap-2">
              <span className="material-symbols-outlined text-primary text-[20px]">person_add</span>
              <span>Create Inbound Lead</span>
            </h2>
            <div>
              <label className="block text-xs font-semibold text-on-surface mb-1">Lead Opportunity Title</label>
              <input
                type="text"
                value={leadTitle}
                onChange={(e) => setLeadTitle(e.target.value)}
                placeholder="e.g. TechCorp Platform Expansion"
                className="w-full bg-surface-container-low text-on-surface text-xs p-2.5 rounded-lg border border-transparent focus:border-primary focus:outline-none"
              />
              {formErrors.title && (
                <p className="text-red-500 text-[11px] mt-1 font-semibold">{formErrors.title}</p>
              )}
            </div>
            <div>
              <label className="block text-xs font-semibold text-on-surface mb-1">Contact Person</label>
              <input
                type="text"
                value={leadContact}
                onChange={(e) => setLeadContact(e.target.value)}
                placeholder="e.g. Alex Rivera"
                className="w-full bg-surface-container-low text-on-surface text-xs p-2.5 rounded-lg border border-transparent focus:border-primary focus:outline-none"
              />
              {formErrors.contactName && (
                <p className="text-red-500 text-[11px] mt-1 font-semibold">{formErrors.contactName}</p>
              )}
            </div>
            <button
              type="submit"
              className="w-full mt-2 bg-primary hover:bg-primary/90 text-on-primary text-xs font-bold py-2.5 rounded-lg shadow-xs"
            >
              Create Lead
            </button>
          </form>
        )}

        {/* DEAL MODAL */}
        {activeModal === 'deal' && (
          <form onSubmit={handleDealSubmit} className="space-y-4">
            <h2 className="text-base font-bold text-on-surface flex items-center gap-2">
              <span className="material-symbols-outlined text-primary text-[20px]">account_tree</span>
              <span>New Pipeline Opportunity</span>
            </h2>
            <div>
              <label className="block text-xs font-semibold text-on-surface mb-1">Deal Name</label>
              <input
                type="text"
                value={dealTitle}
                onChange={(e) => setDealTitle(e.target.value)}
                placeholder="e.g. Enterprise SLA Renewal"
                className="w-full bg-surface-container-low text-on-surface text-xs p-2.5 rounded-lg border border-transparent focus:border-primary focus:outline-none"
              />
              {formErrors.title && (
                <p className="text-red-500 text-[11px] mt-1 font-semibold">{formErrors.title}</p>
              )}
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-xs font-semibold text-on-surface mb-1">Value ($)</label>
                <input
                  type="number"
                  value={dealAmount}
                  onChange={(e) => setDealAmount(e.target.value)}
                  className="w-full bg-surface-container-low text-on-surface text-xs p-2.5 rounded-lg border border-transparent focus:border-primary focus:outline-none"
                />
                {formErrors.amount && (
                  <p className="text-red-500 text-[11px] mt-1 font-semibold">{formErrors.amount}</p>
                )}
              </div>
              <div>
                <label className="block text-xs font-semibold text-on-surface mb-1">Priority</label>
                <select
                  value={dealPriority}
                  onChange={(e) => setDealPriority(e.target.value as any)}
                  className="w-full bg-surface-container-low text-on-surface text-xs p-2.5 rounded-lg border border-transparent focus:border-primary focus:outline-none"
                >
                  <option value="High">High</option>
                  <option value="Medium">Medium</option>
                  <option value="Low">Low</option>
                </select>
              </div>
            </div>
            <button
              type="submit"
              className="w-full mt-2 bg-primary hover:bg-primary/90 text-on-primary text-xs font-bold py-2.5 rounded-lg shadow-xs"
            >
              Add Opportunity
            </button>
          </form>
        )}

        {/* TASK MODAL */}
        {activeModal === 'task' && (
          <form onSubmit={handleTaskSubmit} className="space-y-4">
            <h2 className="text-base font-bold text-on-surface flex items-center gap-2">
              <span className="material-symbols-outlined text-primary text-[20px]">add_task</span>
              <span>Create Task</span>
            </h2>
            <div>
              <label className="block text-xs font-semibold text-on-surface mb-1">Task Action Item</label>
              <input
                type="text"
                value={taskName}
                onChange={(e) => setTaskName(e.target.value)}
                placeholder="e.g. Schedule follow-up demo call"
                className="w-full bg-surface-container-low text-on-surface text-xs p-2.5 rounded-lg border border-transparent focus:border-primary focus:outline-none"
              />
              {formErrors.name && (
                <p className="text-red-500 text-[11px] mt-1 font-semibold">{formErrors.name}</p>
              )}
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-xs font-semibold text-on-surface mb-1">Related Account/Deal</label>
                <input
                  type="text"
                  value={taskDeal}
                  onChange={(e) => setTaskDeal(e.target.value)}
                  className="w-full bg-surface-container-low text-on-surface text-xs p-2.5 rounded-lg border border-transparent focus:border-primary focus:outline-none"
                />
              </div>
              <div>
                <label className="block text-xs font-semibold text-on-surface mb-1">Priority</label>
                <select
                  value={taskPriority}
                  onChange={(e) => setTaskPriority(e.target.value as any)}
                  className="w-full bg-surface-container-low text-on-surface text-xs p-2.5 rounded-lg border border-transparent focus:border-primary focus:outline-none"
                >
                  <option value="High">High</option>
                  <option value="Medium">Medium</option>
                  <option value="Low">Low</option>
                </select>
              </div>
            </div>
            <button
              type="submit"
              className="w-full mt-2 bg-primary hover:bg-primary/90 text-on-primary text-xs font-bold py-2.5 rounded-lg shadow-xs"
            >
              Save Task
            </button>
          </form>
        )}

        {/* INVITE USER MODAL */}
        {activeModal === 'invite' && (
          <form onSubmit={handleUserSubmit} className="space-y-4">
            <h2 className="text-base font-bold text-on-surface flex items-center gap-2">
              <span className="material-symbols-outlined text-primary text-[20px]">person_add</span>
              <span>Invite Team Member</span>
            </h2>
            <div>
              <label className="block text-xs font-semibold text-on-surface mb-1">Full Name</label>
              <input
                type="text"
                value={userName}
                onChange={(e) => setUserName(e.target.value)}
                placeholder="e.g. David Miller"
                className="w-full bg-surface-container-low text-on-surface text-xs p-2.5 rounded-lg border border-transparent focus:border-primary focus:outline-none"
              />
              {formErrors.name && (
                <p className="text-red-500 text-[11px] mt-1 font-semibold">{formErrors.name}</p>
              )}
            </div>
            <div>
              <label className="block text-xs font-semibold text-on-surface mb-1">Work Email</label>
              <input
                type="email"
                value={userEmail}
                onChange={(e) => setUserEmail(e.target.value)}
                placeholder="david.m@company.com"
                className="w-full bg-surface-container-low text-on-surface text-xs p-2.5 rounded-lg border border-transparent focus:border-primary focus:outline-none"
              />
              {formErrors.email && (
                <p className="text-red-500 text-[11px] mt-1 font-semibold">{formErrors.email}</p>
              )}
            </div>
            <div>
              <label className="block text-xs font-semibold text-on-surface mb-1">Role</label>
              <select
                value={userRole}
                onChange={(e) => setUserRole(e.target.value as any)}
                className="w-full bg-surface-container-low text-on-surface text-xs p-2.5 rounded-lg border border-transparent focus:border-primary focus:outline-none"
              >
                <option value="Sales Rep">Sales Rep</option>
                <option value="Manager">Manager</option>
                <option value="Admin">Admin</option>
                <option value="Support">Support</option>
              </select>
            </div>
            <button
              type="submit"
              className="w-full mt-2 bg-primary hover:bg-primary/90 text-on-primary text-xs font-bold py-2.5 rounded-lg shadow-xs"
            >
              Send Invitation
            </button>
          </form>
        )}
      </div>
    </div>
  );
};
