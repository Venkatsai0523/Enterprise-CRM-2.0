import React from 'react';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
} from 'recharts';

const CHANNEL_DATA = [
  { name: 'Inbound Web', value: 45, color: '#004ac6' },
  { name: 'Outbound Sales', value: 25, color: '#2563eb' },
  { name: 'Referrals', value: 20, color: '#6faafd' },
  { name: 'Events / Expos', value: 10, color: '#93c5fd' },
];

const WIN_RATE_DATA = [
  { rep: 'Sarah J.', won: 18, lost: 4 },
  { rep: 'Marcus R.', won: 14, lost: 6 },
  { rep: 'Elena J.', won: 10, lost: 3 },
  { rep: 'Michael J.', won: 8, lost: 2 },
];

export const AnalyticsView: React.FC = () => {
  return (
    <div className="p-4 md:p-6 max-w-7xl mx-auto space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-xl font-bold text-on-surface">Analytics & Performance Reports</h1>
        <p className="text-xs text-on-surface-variant mt-0.5">
          Deep-dive telemetry into revenue velocity, lead sources, and team conversion rates
        </p>
      </div>

      {/* Grid Charts */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Lead Source Distribution */}
        <div className="bg-surface-container-lowest p-5 rounded-2xl border border-border-subtle shadow-xs">
          <h3 className="text-sm font-bold text-on-surface mb-1">Lead Acquisition Channels</h3>
          <p className="text-[11px] text-on-surface-variant mb-4">Inbound vs outbound deal origin</p>
          <div className="h-64 w-full flex items-center justify-center">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={CHANNEL_DATA}
                  cx="50%"
                  cy="50%"
                  innerRadius={60}
                  outerRadius={90}
                  paddingAngle={5}
                  dataKey="value"
                >
                  {CHANNEL_DATA.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} />
                  ))}
                </Pie>
                <Tooltip formatter={(value: number) => [`${value}%`, 'Share']} />
              </PieChart>
            </ResponsiveContainer>
          </div>
          <div className="flex flex-wrap items-center justify-center gap-4 text-xs font-semibold">
            {CHANNEL_DATA.map((ch) => (
              <div key={ch.name} className="flex items-center gap-1.5">
                <span className="w-3 h-3 rounded-full" style={{ backgroundColor: ch.color }} />
                <span className="text-on-surface">{ch.name} ({ch.value}%)</span>
              </div>
            ))}
          </div>
        </div>

        {/* Sales Rep Leaderboard */}
        <div className="bg-surface-container-lowest p-5 rounded-2xl border border-border-subtle shadow-xs">
          <h3 className="text-sm font-bold text-on-surface mb-1">Sales Rep Win vs Loss Rate</h3>
          <p className="text-[11px] text-on-surface-variant mb-4 font-normal">Closed opportunities performance</p>
          <div className="h-64 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={WIN_RATE_DATA}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#E5E7EB" />
                <XAxis dataKey="rep" tickLine={false} tick={{ fontSize: 11 }} />
                <YAxis tickLine={false} tick={{ fontSize: 11 }} />
                <Tooltip />
                <Bar dataKey="won" name="Won Deals" fill="#16A34A" radius={[4, 4, 0, 0]} />
                <Bar dataKey="lost" name="Lost Deals" fill="#DC2626" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>
    </div>
  );
};
