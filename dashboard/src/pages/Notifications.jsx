import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { fetchNotifications, sendTestEmail } from '../api/flowguardApi'

export default function Notifications() {
  const queryClient = useQueryClient()
  const [sent, setSent] = useState(false)

  const { data, isLoading } = useQuery({
    queryKey: ['notifications'],
    queryFn: () => fetchNotifications(),
    refetchInterval: 15000,
  })

  const { mutate: triggerTest, isPending } = useMutation({
    mutationFn: sendTestEmail,
    onSuccess: () => {
      setSent(true)
      queryClient.invalidateQueries({ queryKey: ['notifications'] })
      setTimeout(() => setSent(false), 3000)
    }
  })

  const notifications = data?.content ?? []

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-800">Notifications</h1>
        <button
          onClick={() => triggerTest()}
          disabled={isPending}
          className="bg-brand-600 text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-brand-500 disabled:opacity-50 transition-colors"
        >
          {isPending ? 'Sending…' : sent ? 'Sent!' : 'Send Test Email'}
        </button>
      </div>

      <div className="bg-white rounded-xl border overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-gray-50 text-xs text-gray-500 uppercase">
            <tr>
              {['Type', 'Job', 'Repo', 'Status', 'Sent At'].map(h => (
                <th key={h} className="px-4 py-3 text-left">{h}</th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y">
            {notifications.map(n => (
              <tr key={n.id} className="hover:bg-gray-50">
                <td className="px-4 py-3">
                  <span className={`px-2 py-0.5 rounded text-xs font-medium ${
                    n.alertType === 'REGRESSION' ? 'bg-red-100 text-red-700' :
                    n.alertType === 'FLAKY' ? 'bg-yellow-100 text-yellow-700' :
                    'bg-gray-100 text-gray-600'
                  }`}>
                    {n.alertType}
                  </span>
                </td>
                <td className="px-4 py-3 font-medium">{n.jobName}</td>
                <td className="px-4 py-3 font-mono text-xs text-gray-500">{n.repo}</td>
                <td className="px-4 py-3">
                  <span className={`px-2 py-0.5 rounded text-xs ${
                    n.status === 'SENT' ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'
                  }`}>
                    {n.status}
                  </span>
                </td>
                <td className="px-4 py-3 text-xs text-gray-400">
                  {n.sentAt ? new Date(n.sentAt).toLocaleString() : '—'}
                </td>
              </tr>
            ))}
            {!isLoading && notifications.length === 0 && (
              <tr>
                <td colSpan={5} className="px-4 py-10 text-center text-gray-400">
                  No notifications sent yet.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  )
}
