import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { authService } from '../services/auth.service';

const loginSchema = z.object({
  email: z.string().email('Please enter a valid email address'),
  password: z.string().min(6, 'Password must be at least 6 characters'),
});

type LoginSchema = z.infer<typeof loginSchema>;

interface LoginViewProps {
  onLogin: () => void;
  onGoToOrgSetup: () => void;
}

export const LoginView: React.FC<LoginViewProps> = ({ onLogin, onGoToOrgSetup }) => {
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginSchema>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      email: '',
      password: '',
    },
  });

  const onSubmit = async (data: LoginSchema) => {
    setLoading(true);
    setError(null);
    try {
      await authService.login(data.email, data.password);
      onLogin();
    } catch (err: any) {
      setError(err.message || 'Invalid email or password');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-background flex flex-col justify-center items-center p-4">
      <div className="w-full max-w-md bg-surface-container-lowest rounded-2xl border border-border-subtle shadow-lg p-8 sm:p-10">
        {/* Logo Branding */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-12 h-12 rounded-xl bg-primary text-on-primary mb-3 shadow-md">
            <span className="material-symbols-outlined text-[28px]">hub</span>
          </div>
          <h1 className="text-2xl font-bold text-on-surface">Welcome to NexusCRM</h1>
          <p className="text-xs text-on-surface-variant mt-1">
            Sign in to access your enterprise workspace
          </p>
        </div>

        {error && (
          <div className="mb-6 p-4 bg-error-container text-on-error-container text-xs rounded-xl border border-error-container/20 flex gap-2 items-center animate-shake">
            <span className="material-symbols-outlined text-[18px]">error</span>
            <span>{error}</span>
          </div>
        )}

        {/* Login Form */}
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
          <div>
            <label className="block text-xs font-semibold text-on-surface mb-1.5">
              Work Email Address
            </label>
            <div className="relative">
              <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-on-surface-variant text-[20px]">
                mail
              </span>
              <input
                type="email"
                placeholder="name@company.com"
                {...register('email')}
                className="w-full bg-surface-container-low text-on-surface text-sm pl-10 pr-4 py-2.5 rounded-lg border border-transparent focus:border-primary focus:bg-surface-container-lowest focus:outline-none transition-all"
              />
            </div>
            {errors.email && (
              <p className="text-red-500 text-[11px] mt-1.5 font-medium">{errors.email.message}</p>
            )}
          </div>

          <div>
            <label className="block text-xs font-semibold text-on-surface mb-1.5">
              Password
            </label>
            <div className="relative">
              <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-on-surface-variant text-[20px]">
                lock
              </span>
              <input
                type="password"
                placeholder="••••••••••••"
                {...register('password')}
                className="w-full bg-surface-container-low text-on-surface text-sm pl-10 pr-4 py-2.5 rounded-lg border border-transparent focus:border-primary focus:bg-surface-container-lowest focus:outline-none transition-all"
              />
            </div>
            {errors.password && (
              <p className="text-red-500 text-[11px] mt-1.5 font-medium">{errors.password.message}</p>
            )}
          </div>

          <div className="flex items-center justify-between text-xs">
            <label className="flex items-center gap-2 text-on-surface-variant cursor-pointer select-none">
              <input
                type="checkbox"
                className="rounded text-primary focus:ring-primary h-4 w-4"
              />
              <span>Remember this device</span>
            </label>
            <button
              type="button"
              className="text-primary font-medium hover:underline focus:outline-none"
            >
              Forgot password?
            </button>
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-primary hover:bg-primary/90 text-on-primary font-semibold text-sm py-3 rounded-lg shadow-md hover:shadow-lg transition-all flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <span>{loading ? 'Signing In...' : 'Sign In to Workspace'}</span>
            <span className="material-symbols-outlined text-[18px]">arrow_forward</span>
          </button>
        </form>

        {/* Footer Link */}
        <div className="mt-8 pt-6 border-t border-t-border-subtle text-center text-xs text-on-surface-variant">
          <span>Need a new CRM organization? </span>
          <button
            onClick={onGoToOrgSetup}
            className="text-primary font-semibold hover:underline"
          >
            Register Organization
          </button>
        </div>
      </div>
    </div>
  );
};
