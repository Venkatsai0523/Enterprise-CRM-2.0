import { Customer, Lead, Deal, Task, Activity, User, NotificationItem } from './types';

const BASE_URL = 'http://localhost:8080/api';

// Helper to get authorization headers
export function getAuthHeaders(): HeadersInit {
  const token = localStorage.getItem('token');
  const headers: HeadersInit = {
    'Content-Type': 'application/json',
  };
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  return headers;
}

// Global API request wrapper
async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const url = `${BASE_URL}${path}`;
  const response = await fetch(url, {
    ...options,
    headers: {
      ...getAuthHeaders(),
      ...options.headers,
    },
  });

  if (response.status === 204) {
    return {} as T;
  }

  const json = await response.json();
  if (!response.ok) {
    throw new Error(json.message || 'An error occurred processing the request');
  }

  // Extract from standardized response wrapper: StandardResponse<T> -> $.data
  return json.data as T;
}

// Logo BG utilities for UI mapping
function getLogoBg(name: string): string {
  const bgs = [
    'bg-blue-100 text-blue-700 border-blue-200',
    'bg-purple-100 text-purple-700 border-purple-200',
    'bg-red-100 text-red-700 border-red-200',
    'bg-teal-100 text-teal-700 border-teal-200',
    'bg-amber-100 text-amber-700 border-amber-200',
  ];
  let hash = 0;
  for (let i = 0; i < name.length; i++) {
    hash = name.charCodeAt(i) + ((hash << 5) - hash);
  }
  return bgs[Math.abs(hash) % bgs.length];
}

// Mappers from Backend DTO to Frontend Types
export function mapCustomer(dto: any): Customer {
  return {
    id: dto.id,
    name: dto.accountName,
    domain: dto.domainName,
    logoText: dto.accountName ? dto.accountName.split(' ').map((w: string) => w[0]).join('').substring(0, 2).toUpperCase() : 'C',
    logoBg: getLogoBg(dto.accountName || 'Customer'),
    status: dto.status === 'ACTIVE' ? 'Active' : (dto.status === 'ONBOARDING' ? 'Onboarding' : 'At Risk'),
    industry: 'Enterprise Software',
    mainContact: {
      name: dto.primaryEmail ? dto.primaryEmail.split('@')[0] : 'Contact',
      email: dto.primaryEmail || '',
      phone: dto.phone || '',
      avatar: 'https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=150&auto=format&fit=crop&q=80',
    },
    totalValue: dto.totalLifetimeValue || 0,
    lastActivity: 'Recently',
    accountOwner: {
      name: 'Account Manager',
      avatarText: 'AM',
    },
    arr: dto.totalLifetimeValue || 0,
    renewalDate: dto.updatedAt ? new Date(dto.updatedAt).toLocaleDateString() : 'N/A',
    tags: ['Customer'],
    healthScore: dto.status === 'ACTIVE' ? 85 : 68,
    healthTrend: 'Stable',
    location: 'United States',
  };
}

export function mapLead(dto: any): Lead {
  return {
    id: dto.id,
    title: `${dto.companyName || 'New'} Lead`,
    contactName: `${dto.firstName || ''} ${dto.lastName || ''}`.trim() || 'Unnamed',
    date: dto.createdAt ? new Date(dto.createdAt).toLocaleDateString() : 'N/A',
    score: dto.score || 0,
    status: dto.status === 'NEW' ? 'New Leads' : (dto.status === 'CONTACTED' ? 'Contacted' : (dto.status === 'QUALIFIED' ? 'Qualified' : 'Nurturing')),
    owner: dto.assignedRepId || 'Unassigned',
    isHot: (dto.score || 0) >= 80,
  };
}

export function mapDeal(dto: any): Deal {
  return {
    id: dto.id,
    title: dto.title,
    company: 'Associated Organization',
    amount: dto.estimatedValue || 0,
    priority: (dto.estimatedValue || 0) > 100000 ? 'High' : ((dto.estimatedValue || 0) > 50000 ? 'Medium' : 'Low'),
    stage: dto.stage === 'PROSPECTING' ? 'Prospect' : (dto.stage === 'QUALIFIED' ? 'Qualified' : (dto.stage === 'PROPOSAL' ? 'Proposal' : (dto.stage === 'NEGOTIATION' ? 'Negotiation' : 'Closed Won'))),
    dueDate: dto.updatedAt ? new Date(dto.updatedAt).toLocaleDateString() : 'N/A',
    owner: {
      name: 'Sales Rep',
      avatar: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150&auto=format&fit=crop&q=80',
    },
  };
}

export function mapTask(dto: any): Task {
  return {
    id: dto.id,
    name: dto.title,
    deal: 'Related Deal',
    priority: dto.priority === 'HIGH' ? 'High' : (dto.priority === 'MEDIUM' ? 'Medium' : 'Low'),
    dueDate: dto.dueDate ? new Date(dto.dueDate).toLocaleDateString() : 'N/A',
    dueStatus: dto.dueDate && new Date(dto.dueDate) < new Date() ? 'Overdue' : 'Due',
    assignee: {
      name: 'Rep',
      avatar: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150&auto=format&fit=crop&q=80',
    },
    progress: dto.status === 'COMPLETED' ? 100 : 0,
    completed: dto.status === 'COMPLETED',
  };
}

export function mapNotification(dto: any): NotificationItem {
  return {
    id: dto.id,
    type: dto.type === 'TASK_ASSIGNED' ? 'task' : (dto.type === 'DEAL_WON' ? 'deal' : 'system'),
    category: dto.read ? 'Read' : 'Unread',
    title: dto.type ? dto.type.replace('_', ' ') : 'Notification',
    body: dto.message || '',
    time: dto.createdAt ? new Date(dto.createdAt).toLocaleTimeString() : 'Just now',
    read: dto.read,
  };
}

// API Service Endpoints
export const api = {
  // Auth & Admin
  async login(email: string, passwordHash: string): Promise<any> {
    const data = await request<any>('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password: passwordHash }),
    });
    if (data.token) {
      localStorage.setItem('token', data.token);
      localStorage.setItem('refreshToken', data.refreshToken);
      localStorage.setItem('role', data.roles?.[0] || 'ROLE_SALES_REP');
      localStorage.setItem('email', data.email);
    }
    return data;
  },

  async register(email: string, passwordHash: string, firstName: string, lastName: string, roleName: string, organizationId?: string): Promise<any> {
    return request<any>('/auth/register', {
      method: 'POST',
      body: JSON.stringify({ email, password: passwordHash, firstName, lastName, roleName, organizationId }),
    });
  },

  async getMe(): Promise<any> {
    return request<any>('/users/me');
  },

  // Organizations
  async lookupOrg(subdomain: string): Promise<any> {
    return request<any>(`/organizations/lookup?subdomain=${subdomain}`);
  },

  async createOrg(name: string, subdomain: string): Promise<any> {
    return request<any>('/organizations', {
      method: 'POST',
      body: JSON.stringify({ name, subdomain }),
    });
  },

  // Leads
  async getLeads(): Promise<Lead[]> {
    const data = await request<any>('/leads?size=100');
    const content = data.content || [];
    return content.map(mapLead);
  },

  async createLead(lead: any): Promise<Lead> {
    const res = await request<any>('/leads', {
      method: 'POST',
      body: JSON.stringify(lead),
    });
    return mapLead(res);
  },

  async updateLead(id: string, lead: any): Promise<Lead> {
    const res = await request<any>(`/leads/${id}`, {
      method: 'PUT',
      body: JSON.stringify(lead),
    });
    return mapLead(res);
  },

  async deleteLead(id: string): Promise<void> {
    return request<void>(`/leads/${id}`, {
      method: 'DELETE',
    });
  },

  // Opportunities (Deals)
  async getDeals(): Promise<Deal[]> {
    const data = await request<any>('/opportunities?size=100');
    const content = data.content || [];
    return content.map(mapDeal);
  },

  async createDeal(deal: any): Promise<Deal> {
    const res = await request<any>('/opportunities', {
      method: 'POST',
      body: JSON.stringify(deal),
    });
    return mapDeal(res);
  },

  async updateDealStage(id: string, stage: string): Promise<Deal> {
    const res = await request<any>(`/opportunities/${id}/stage`, {
      method: 'PATCH',
      body: JSON.stringify({ stage }),
    });
    return mapDeal(res);
  },

  // Customers
  async getCustomers(): Promise<Customer[]> {
    const data = await request<any>('/customers?size=100');
    const content = data.content || [];
    return content.map(mapCustomer);
  },

  async getCustomer360(id: string): Promise<any> {
    const res = await request<any>(`/customers/${id}`);
    return {
      customer: mapCustomer(res),
      activities: (res.activities || []).map(mapTask),
      linkedOpportunities: (res.linkedOpportunities || []).map(mapDeal),
    };
  },

  // Tasks
  async getTasks(): Promise<Task[]> {
    try {
      const dashboard = await this.getDashboard();
      return dashboard.tasks || [];
    } catch {
      return [];
    }
  },

  async createTask(task: any): Promise<Task> {
    const res = await request<any>('/tasks', {
      method: 'POST',
      body: JSON.stringify(task),
    });
    return mapTask(res);
  },

  async updateTaskStatus(id: string, status: string): Promise<Task> {
    const res = await request<any>(`/tasks/${id}/status?status=${status}`, {
      method: 'PATCH',
    });
    return mapTask(res);
  },

  // Notifications
  async getNotifications(): Promise<NotificationItem[]> {
    let userId = localStorage.getItem('userId');
    if (!userId) {
      try {
        const me = await this.getMe();
        userId = me.id;
        localStorage.setItem('userId', me.id);
      } catch {
        userId = 'c0000000-0000-0000-0000-000000000002'; // default seed rep ID
      }
    }
    const data = await request<any>(`/notifications?recipientId=${userId}&size=100`);
    const content = data.content || [];
    return content.map(mapNotification);
  },

  async markNotificationRead(id: string): Promise<void> {
    return request<void>(`/notifications/${id}/read`, {
      method: 'PATCH',
    });
  },

  // Dashboard Analytics
  async getDashboard(): Promise<any> {
    return request<any>('/analytics/dashboard');
  },
};
