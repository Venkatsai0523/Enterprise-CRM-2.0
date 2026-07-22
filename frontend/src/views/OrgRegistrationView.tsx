import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { organizationService } from '../services/organization.service';
import { authService } from '../services/auth.service';

const orgSchema = z.object({
  orgName: z.string().min(2, 'Organization name must be at least 2 characters'),
  slug: z.string().min(2, 'Subdomain slug must be at least 2 characters').regex(/^[a-z0-9-]+$/, 'Slug must contain only lowercase letters, numbers, and hyphens'),
  industry: z.string().default('Enterprise Software'),
  plan: z.enum(['starter', 'pro', 'enterprise']).default('pro'),
  adminName: z.string().min(2, 'Administrator name is required'),
  adminEmail: z.string().email('Please enter a valid email address'),
  adminPassword: z.string().min(6, 'Password must be at least 6 characters'),
});

type OrgSchema = z.infer<typeof orgSchema>;

interface OrgRegistrationViewProps {
  onComplete: () => void;
  onCancel: () => void;
}

export const OrgRegistrationView: React.FC<OrgRegistrationViewProps> = ({
  onComplete,
  onCancel,
}) => {
  const [step, setStep] = useState<1 | 2>(1);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const {
    register,
    handleSubmit,
    trigger,
    setValue,
    watch,
    formState: { errors },
  } = useForm<OrgSchema>({
    resolver: zodResolver(orgSchema),
    defaultValues: {
      orgName: '',
      slug: '',
      industry: 'Enterprise Software',
      plan: 'pro',
      adminName: '',
      adminEmail: '',
      adminPassword: '',
    },
  });

  const orgNameVal = watch('orgName');
  const slugVal = watch('slug');
  const planVal = watch('plan');
  const industryVal = watch('industry');

  const handleOrgNameChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const val = e.target.value;
    setValue('orgName', val);
    setValue('slug', val.toLowerCase().replace(/[^a-z0-9]/g, '-').replace(/-+/g, '-'));
  };

  const handleNextStep = async () => {
    const isValid = await trigger(['orgName', 'slug']);
    if (isValid) {
      setError(null);
      setStep(2);
    }
  };

  const onSubmit = async (data: OrgSchema) => {
    setLoading(true);
    setError(null);
    try {
      // 1. Create Organization
      const org = await organizationService.createOrganization(data.orgName, data.slug);
      
      // 2. Register Super Admin User
      const names = data.adminName.split(' ');
      const firstName = names[0] || 'Admin';
      const lastName = names.slice(1).join(' ') || 'User';
      
      await authService.register(
        data.adminEmail,
        data.adminPassword,
        firstName,
        lastName,
        'ROLE_ADMIN',
        org.id
      );

      // 3. Eager login
      await authService.login(data.adminEmail, data.adminPassword);
      onComplete();
    } catch (err: any) {
      setError(err.message || 'Failed to register organization');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-background flex flex-col justify-center items-center p-4 py-8">
      <div className="w-full max-w-2xl bg-surface-container-lowest rounded-2xl border border-border-subtle shadow-xl p-6 sm:p-10">
        {/* Header */}
        <div className="flex items-center justify-between mb-8 pb-4 border-b border-border-subtle">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-primary text-on-primary flex items-center justify-center font-bold">
              <span className="material-symbols-outlined text-[24px]">corporate_fare</span>
            </div>
            <div>
              <h1 className="text-xl font-bold text-on-surface">Organization Setup</h1>
              <p className="text-xs text-on-surface-variant">Step {step} of 2 - Configure your CRM Workspace</p>
            </div>
          </div>
          <button
            onClick={onCancel}
            className="text-xs font-semibold text-on-surface-variant hover:text-on-surface hover:underline"
          >
            Back to Login
          </button>
        </div>

        {/* Progress Bar */}
        <div className="w-full bg-surface-container-low h-2 rounded-full mb-8 overflow-hidden">
          <div
            className="bg-primary h-full transition-all duration-300"
            style={{ width: step === 1 ? '50%' : '100%' }}
          />
        </div>

        {error && (
          <div className="mb-6 p-4 bg-error-container text-on-error-container text-xs rounded-xl border border-error-container/20 flex gap-2 items-center">
            <span className="material-symbols-outlined text-[18px]">error</span>
            <span>{error}</span>
          </div>
        )}

        {step === 1 ? (
          /* Step 1: Org Info */
          <div className="space-y-6">
            <h2 className="text-base font-semibold text-on-surface">Company Details & Workspace URL</h2>

            <div className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-on-surface mb-1">
                  Organization Name
                </label>
                <input
                  type="text"
                  value={orgNameVal}
                  onChange={handleOrgNameChange}
                  placeholder="e.g. Acme Corporation"
                  className="w-full bg-surface-container-low text-on-surface text-sm px-4 py-2.5 rounded-lg border border-transparent focus:border-primary focus:bg-surface-container-lowest focus:outline-none"
                />
                {errors.orgName && (
                  <p className="text-red-500 text-[11px] mt-1.5 font-medium">{errors.orgName.message}</p>
                )}
              </div>

              <div>
                <label className="block text-xs font-semibold text-on-surface mb-1">
                  Workspace Subdomain
                </label>
                <div className="flex items-center">
                  <span className="bg-surface-container text-on-surface-variant text-xs px-3 py-2.5 rounded-l-lg border border-r-0 border-border-subtle font-mono">
                    https://
                  </span>
                  <input
                    type="text"
                    value={slugVal}
                    onChange={(e) => setValue('slug', e.target.value)}
                    className="flex-1 bg-surface-container-low text-on-surface text-sm px-3 py-2.5 border border-transparent focus:border-primary focus:bg-surface-container-lowest focus:outline-none font-mono"
                  />
                  <span className="bg-surface-container text-on-surface-variant text-xs px-3 py-2.5 rounded-r-lg border border-l-0 border-border-subtle font-mono">
                    .nexuscrm.com
                  </span>
                </div>
                {errors.slug && (
                  <p className="text-red-500 text-[11px] mt-1.5 font-medium">{errors.slug.message}</p>
                )}
              </div>

              <div>
                <label className="block text-xs font-semibold text-on-surface mb-1">
                  Primary Industry
                </label>
                <select
                  value={industryVal}
                  onChange={(e) => setValue('industry', e.target.value)}
                  className="w-full bg-surface-container-low text-on-surface text-sm px-4 py-2.5 rounded-lg border border-transparent focus:border-primary focus:bg-surface-container-lowest focus:outline-none"
                >
                  <option>Enterprise Software</option>
                  <option>Financial Services & Banking</option>
                  <option>Healthcare & Biotech</option>
                  <option>Manufacturing & Logistics</option>
                  <option>Retail & E-commerce</option>
                </select>
              </div>

              <div>
                <label className="block text-xs font-semibold text-on-surface mb-2">
                  Select Subscription Plan
                </label>
                <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                  {[
                    { id: 'starter', name: 'Starter', price: '$29/mo', seats: 'Up to 5 seats' },
                    { id: 'pro', name: 'Pro Business', price: '$79/mo', seats: 'Up to 25 seats' },
                    { id: 'enterprise', name: 'Enterprise', price: '$199/mo', seats: 'Unlimited seats' },
                  ].map((p) => (
                    <button
                      key={p.id}
                      type="button"
                      onClick={() => setValue('plan', p.id as any)}
                      className={`p-3.5 rounded-xl border text-left transition-all ${
                        planVal === p.id
                          ? 'border-primary bg-primary-container/10 ring-2 ring-primary/20'
                          : 'border-border-subtle hover:bg-surface-container-low'
                      }`}
                    >
                      <p className="text-xs font-bold text-on-surface">{p.name}</p>
                      <p className="text-sm font-extrabold text-primary my-1">{p.price}</p>
                      <p className="text-[11px] text-on-surface-variant">{p.seats}</p>
                    </button>
                  ))}
                </div>
              </div>
            </div>

            <div className="pt-4 flex justify-end">
              <button
                type="button"
                onClick={handleNextStep}
                className="bg-primary hover:bg-primary/90 text-on-primary font-semibold text-sm px-6 py-2.5 rounded-lg shadow-sm flex items-center gap-2"
              >
                <span>Continue to Admin Setup</span>
                <span className="material-symbols-outlined text-[18px]">arrow_forward</span>
              </button>
            </div>
          </div>
        ) : (
          /* Step 2: Admin Info */
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
            <h2 className="text-base font-semibold text-on-surface">System Administrator Credentials</h2>

            <div className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-on-surface mb-1">
                  Admin Full Name
                </label>
                <input
                  type="text"
                  placeholder="Sarah Jenkins"
                  {...register('adminName')}
                  className="w-full bg-surface-container-low text-on-surface text-sm px-4 py-2.5 rounded-lg border border-transparent focus:border-primary focus:bg-surface-container-lowest focus:outline-none"
                />
                {errors.adminName && (
                  <p className="text-red-500 text-[11px] mt-1.5 font-medium">{errors.adminName.message}</p>
                )}
              </div>

              <div>
                <label className="block text-xs font-semibold text-on-surface mb-1">
                  Work Email (Super Admin User)
                </label>
                <input
                  type="email"
                  placeholder="admin@company.com"
                  {...register('adminEmail')}
                  className="w-full bg-surface-container-low text-on-surface text-sm px-4 py-2.5 rounded-lg border border-transparent focus:border-primary focus:bg-surface-container-lowest focus:outline-none"
                />
                {errors.adminEmail && (
                  <p className="text-red-500 text-[11px] mt-1.5 font-medium">{errors.adminEmail.message}</p>
                )}
              </div>

              <div>
                <label className="block text-xs font-semibold text-on-surface mb-1">
                  Admin Password
                </label>
                <input
                  type="password"
                  placeholder="Admin123"
                  {...register('adminPassword')}
                  className="w-full bg-surface-container-low text-on-surface text-sm px-4 py-2.5 rounded-lg border border-transparent focus:border-primary focus:bg-surface-container-lowest focus:outline-none"
                />
                {errors.adminPassword && (
                  <p className="text-red-500 text-[11px] mt-1.5 font-medium">{errors.adminPassword.message}</p>
                )}
              </div>
            </div>

            <div className="p-4 rounded-xl bg-surface-container-low border border-border-subtle text-xs text-on-surface-variant flex gap-3 items-center">
              <span className="material-symbols-outlined text-primary text-[24px]">verified_user</span>
              <p>
                By clicking launch, your NexusCRM workspace will be provisioned with full administrative access and sample CRM workflows.
              </p>
            </div>

            <div className="pt-4 flex items-center justify-between">
              <button
                type="button"
                onClick={() => setStep(1)}
                className="text-xs font-semibold text-on-surface-variant hover:text-on-surface"
              >
                Back
              </button>
              <button
                type="submit"
                disabled={loading}
                className="bg-primary hover:bg-primary/90 text-on-primary font-semibold text-sm px-6 py-2.5 rounded-lg shadow-md flex items-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                <span>{loading ? 'Launching Workspace...' : 'Launch Organization Workspace'}</span>
                <span className="material-symbols-outlined text-[18px]">rocket_launch</span>
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
};
