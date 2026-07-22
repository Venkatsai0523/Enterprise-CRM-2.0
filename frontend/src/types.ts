export type ViewMode =
  | 'login'
  | 'org-setup'
  | 'dashboard'
  | 'customers'
  | 'customer-detail'
  | 'leads'
  | 'pipeline'
  | 'tasks'
  | 'analytics'
  | 'settings'
  | 'notifications';

export interface User {
  id: string;
  name: string;
  email: string;
  role: 'Admin' | 'Manager' | 'Sales Rep' | 'Support';
  team: 'Product' | 'Enterprise Sales' | 'Customer Success' | 'Engineering';
  status: 'Active' | 'Pending' | 'Deactivated';
  avatar?: string;
  avatarText?: string;
  lastActive: string;
}

export interface Customer {
  id: string;
  name: string;
  domain: string;
  logoText: string;
  logoBg: string;
  status: 'Active' | 'Onboarding' | 'At Risk';
  industry: string;
  mainContact: {
    name: string;
    email: string;
    phone: string;
    avatar: string;
  };
  totalValue: number;
  lastActivity: string;
  accountOwner: {
    name: string;
    avatarText: string;
  };
  arr: number;
  renewalDate: string;
  tags: string[];
  healthScore: number;
  healthTrend: string;
  location: string;
}

export interface Lead {
  id: string;
  title: string;
  contactName: string;
  date: string;
  score: number;
  status: 'New Leads' | 'Contacted' | 'Qualified' | 'Nurturing';
  owner: string;
  isHot?: boolean;
}

export interface Deal {
  id: string;
  title: string;
  company: string;
  amount: number;
  priority: 'Low' | 'Medium' | 'High';
  stage: 'Prospect' | 'Qualified' | 'Proposal' | 'Negotiation' | 'Closed Won';
  dueDate: string;
  owner: {
    name: string;
    avatar: string;
  };
}

export interface Task {
  id: string;
  name: string;
  deal: string;
  priority: 'Low' | 'Medium' | 'High';
  dueDate: string;
  dueStatus?: string;
  assignee: {
    name: string;
    avatar: string;
  };
  progress: number;
  completed: boolean;
  time?: string;
  isHighPriorityCard?: boolean;
}

export interface Activity {
  id: string;
  type: 'email' | 'call' | 'meeting' | 'stage_change';
  author: string;
  target?: string;
  title: string;
  content: string;
  time: string;
  notes?: string;
  dealName?: string;
  newStage?: string;
}

export interface NotificationItem {
  id: string;
  type: 'mention' | 'system' | 'deal' | 'task';
  category: 'Unread' | 'Mentions' | 'System' | 'Read';
  title: string;
  subtitle?: string;
  body: string;
  time: string;
  read: boolean;
  userAvatar?: string;
  iconName?: string;
}
