import React, { useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import {
  ViewMode,
  Customer,
  Lead,
  Deal,
  Task,
  Activity,
  User,
  NotificationItem,
} from './types';
import { Sidebar } from './components/Sidebar';
import { TopBar } from './components/TopBar';
import { Modals } from './components/Modals';

import { LoginView } from './views/LoginView';
import { OrgRegistrationView } from './views/OrgRegistrationView';
import { DashboardOverview } from './views/DashboardOverview';
import { CustomersView } from './views/CustomersView';
import { CustomerDetailView } from './views/CustomerDetailView';
import { LeadsView } from './views/LeadsView';
import { PipelineView } from './views/PipelineView';
import { TasksView } from './views/TasksView';
import { UserManagementView } from './views/UserManagementView';
import { NotificationsView } from './views/NotificationsView';
import { AnalyticsView } from './views/AnalyticsView';

import { mapCustomer, mapLead, mapDeal, mapTask, mapNotification } from './api';
import { userService } from './services/user.service';
import { leadService } from './services/lead.service';
import { opportunityService } from './services/opportunity.service';
import { customerService } from './services/customer.service';
import { taskService } from './services/task.service';
import { notificationService } from './services/notification.service';
import { dashboardService } from './services/dashboard.service';

export function App() {
  const queryClient = useQueryClient();
  const [currentView, setCurrentView] = useState<ViewMode>(
    localStorage.getItem('token') ? 'dashboard' : 'login'
  );

  const [activities, setActivities] = useState<Activity[]>([]);
  const [users, setUsers] = useState<User[]>([]);
  const [selectedCustomer, setSelectedCustomer] = useState<Customer | null>(null);
  const [activeModal, setActiveModal] = useState<'customer' | 'lead' | 'deal' | 'task' | 'invite' | null>(null);
  const [isOpenMobile, setIsOpenMobile] = useState(false);
  const [globalSearch, setGlobalSearch] = useState('');

  const isAuth = currentView !== 'login' && currentView !== 'org-setup';

  // 1. Current User
  const { data: currentUser } = useQuery({
    queryKey: ['me'],
    queryFn: () => userService.getMe(),
    enabled: isAuth,
  });

  // 2. Customers
  const { data: customers = [] } = useQuery({
    queryKey: ['customers'],
    queryFn: async () => {
      const res = await customerService.getCustomers(0, 100);
      return (res.content || []).map(mapCustomer);
    },
    enabled: isAuth,
  });

  // 3. Leads
  const { data: leads = [] } = useQuery({
    queryKey: ['leads'],
    queryFn: async () => {
      const res = await leadService.getLeads({ size: 100 });
      return (res.content || []).map(mapLead);
    },
    enabled: isAuth,
  });

  // 4. Deals (Opportunities)
  const { data: deals = [] } = useQuery({
    queryKey: ['deals'],
    queryFn: async () => {
      const res = await opportunityService.getOpportunities({ size: 100 });
      return (res.content || []).map(mapDeal);
    },
    enabled: isAuth,
  });

  // 5. Tasks (from dashboard endpoint)
  const { data: tasks = [] } = useQuery({
    queryKey: ['tasks'],
    queryFn: async () => {
      const res = await dashboardService.getDashboardMetrics();
      return (res.tasks || []).map(mapTask);
    },
    enabled: isAuth,
  });

  // 6. Notifications
  const { data: notifications = [] } = useQuery({
    queryKey: ['notifications'],
    queryFn: async () => {
      const me = await userService.getMe();
      const res = await notificationService.getNotifications({ recipientId: me.id, size: 100 });
      return (res.content || []).map(mapNotification);
    },
    enabled: isAuth,
  });

  const unreadCount = notifications.filter((n) => !n.read).length;

  // Handlers
  const handleSelectCustomer = async (customer: Customer) => {
    try {
      const data = await customerService.getCustomer360(customer.id);
      setSelectedCustomer(mapCustomer(data));
      setActivities((data.activities || []).map((t: any) => ({
        id: t.id,
        type: 'meeting',
        author: 'System',
        title: t.name,
        content: 'CRM Action Item',
        time: t.dueDate
      })));
      setCurrentView('customer-detail');
    } catch (err) {
      console.error(err);
      setSelectedCustomer(customer);
      setCurrentView('customer-detail');
    }
  };

  const handleAddCustomer = (newCust: Customer) => {
    alert("In this CRM system, Customer Accounts are automatically created when an Opportunity/Deal is transitioned to the 'Closed Won' stage.");
  };

  const handleAddLead = async (newLead: Lead) => {
    try {
      const firstName = newLead.contactName.split(' ')[0] || '';
      const lastName = newLead.contactName.split(' ')[1] || '';
      await leadService.createLead({
        firstName,
        lastName,
        email: `${firstName.toLowerCase()}@${newLead.title.toLowerCase().replace(/[^a-z0-9]/g, '')}.com`,
        companyName: newLead.title.replace(' Lead', ''),
        companySize: '100-500',
        leadSource: 'WEBSITE'
      });
      queryClient.invalidateQueries({ queryKey: ['leads'] });
    } catch (err) {
      console.error(err);
    }
  };

  const handleAddDeal = async (newDeal: Deal) => {
    try {
      await opportunityService.createOpportunity({
        title: newDeal.title,
        leadId: leads[0]?.id || 'e8888888-8888-8888-8888-888888888888',
        estimatedValue: newDeal.amount
      });
      queryClient.invalidateQueries({ queryKey: ['deals'] });
    } catch (err) {
      console.error(err);
    }
  };

  const handleAddTask = async (newTask: Task) => {
    try {
      const dateStr = new Date().toISOString().split('T')[0];
      await taskService.createTask({
        title: newTask.name,
        priority: newTask.priority === 'High' ? 'HIGH' : (newTask.priority === 'Medium' ? 'MEDIUM' : 'LOW'),
        dueDate: dateStr
      });
      queryClient.invalidateQueries({ queryKey: ['tasks'] });
    } catch (err) {
      console.error(err);
    }
  };

  const handleAddUser = (newUser: User) => {
    setUsers([newUser, ...users]);
  };

  const handleAddActivity = (newAct: Activity) => {
    setActivities([newAct, ...activities]);
  };

  const handleToggleTask = async (taskId: string) => {
    try {
      const targetTask = tasks.find((t) => t.id === taskId);
      if (targetTask) {
        const newStatus = targetTask.completed ? 'TODO' : 'COMPLETED';
        await taskService.patchTaskStatus(taskId, newStatus);
        queryClient.invalidateQueries({ queryKey: ['tasks'] });
      }
    } catch (err) {
      console.error(err);
    }
  };

  const handleMoveDealStage = async (dealId: string, newStage: Deal['stage']) => {
    try {
      const backendStage = newStage === 'Prospect' ? 'PROSPECTING' : (newStage === 'Qualified' ? 'QUALIFIED' : (newStage === 'Proposal' ? 'PROPOSAL' : (newStage === 'Negotiation' ? 'NEGOTIATION' : 'WON')));
      await opportunityService.patchOpportunityStage(dealId, backendStage);
      queryClient.invalidateQueries({ queryKey: ['deals'] });
      if (backendStage === 'WON') {
        queryClient.invalidateQueries({ queryKey: ['customers'] });
      }
    } catch (err) {
      console.error(err);
    }
  };

  const handleMoveLeadStage = async (leadId: string, newStatus: Lead['status']) => {
    try {
      const backendStatus = newStatus === 'New Leads' ? 'NEW' : (newStatus === 'Contacted' ? 'CONTACTED' : (newStatus === 'Qualified' ? 'QUALIFIED' : 'NURTURING'));
      await leadService.updateLead(leadId, { status: backendStatus });
      queryClient.invalidateQueries({ queryKey: ['leads'] });
    } catch (err) {
      console.error(err);
    }
  };

  const handleUpdateUserStatus = (userId: string, newStatus: User['status']) => {
    setUsers(users.map((u) => (u.id === userId ? { ...u, status: newStatus } : u)));
  };

  const handleMarkAllRead = async () => {
    try {
      await Promise.all(
        notifications.filter((n) => !n.read).map((n) => notificationService.patchNotificationRead(n.id))
      );
      queryClient.invalidateQueries({ queryKey: ['notifications'] });
    } catch (err) {
      console.error(err);
    }
  };

  // Full-screen flows without sidebar/topbar layout
  if (currentView === 'login') {
    return (
      <LoginView
        onLogin={() => setCurrentView('dashboard')}
        onGoToOrgSetup={() => setCurrentView('org-setup')}
      />
    );
  }

  if (currentView === 'org-setup') {
    return (
      <OrgRegistrationView
        onComplete={() => setCurrentView('dashboard')}
        onCancel={() => setCurrentView('login')}
      />
    );
  }

  return (
    <div className="min-h-screen bg-background text-on-surface font-sans antialiased flex">
      {/* Sidebar Navigation */}
      <Sidebar
        currentView={currentView}
        onNavigate={setCurrentView}
        unreadCount={unreadCount}
        isOpenMobile={isOpenMobile}
        onCloseMobile={() => setIsOpenMobile(false)}
      />

      {/* Main Content Area */}
      <div className="flex-1 md:pl-sidebar-width flex flex-col min-w-0 min-h-screen">
        {/* Top Sticky Header */}
        <TopBar
          currentView={currentView}
          onNavigate={setCurrentView}
          unreadCount={unreadCount}
          onOpenMobile={() => setIsOpenMobile(true)}
          searchTerm={globalSearch}
          onSearchChange={(e) => setGlobalSearch(e.target.value)}
          currentUser={currentUser}
        />

        {/* Dynamic Page Views */}
        <main className="flex-1 pb-12">
          {currentView === 'dashboard' && (
            <DashboardOverview
              customers={customers}
              tasks={tasks}
              activities={activities}
              onNavigate={setCurrentView}
              onOpenModal={setActiveModal}
              userName={currentUser ? currentUser.firstName : ''}
              deals={deals}
              leads={leads}
            />
          )}

          {currentView === 'customers' && (
            <CustomersView
              customers={customers}
              onSelectCustomer={handleSelectCustomer}
              onOpenNewCustomerModal={() => setActiveModal('customer')}
            />
          )}

          {currentView === 'customer-detail' && selectedCustomer && (
            <CustomerDetailView
              customer={selectedCustomer}
              activities={activities}
              tasks={tasks}
              onNavigate={setCurrentView}
              onAddActivity={handleAddActivity}
              onOpenTaskModal={() => setActiveModal('task')}
            />
          )}

          {currentView === 'leads' && (
            <LeadsView
              leads={leads}
              onMoveLeadStage={handleMoveLeadStage}
              onOpenNewLeadModal={() => setActiveModal('lead')}
            />
          )}

          {currentView === 'pipeline' && (
            <PipelineView
              deals={deals}
              onMoveDealStage={handleMoveDealStage}
              onOpenNewDealModal={() => setActiveModal('deal')}
            />
          )}

          {currentView === 'tasks' && (
            <TasksView
              tasks={tasks}
              onToggleTask={handleToggleTask}
              onOpenNewTaskModal={() => setActiveModal('task')}
            />
          )}

          {currentView === 'analytics' && <AnalyticsView />}

          {currentView === 'settings' && (
            <UserManagementView
              users={users}
              onOpenInviteModal={() => setActiveModal('invite')}
              onUpdateUserStatus={handleUpdateUserStatus}
            />
          )}

          {currentView === 'notifications' && (
            <NotificationsView
              notifications={notifications}
              onMarkAllRead={handleMarkAllRead}
              onNavigate={setCurrentView}
            />
          )}
        </main>
      </div>

      {/* Global Modals */}
      <Modals
        activeModal={activeModal}
        onClose={() => setActiveModal(null)}
        onAddCustomer={handleAddCustomer}
        onAddLead={handleAddLead}
        onAddDeal={handleAddDeal}
        onAddTask={handleAddTask}
        onAddUser={handleAddUser}
      />
    </div>
  );
}

export default App;
