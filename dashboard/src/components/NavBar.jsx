import { NavLink } from 'react-router-dom'

const links = [
  { to: '/', label: 'Overview' },
  { to: '/trends', label: 'Job Trends' },
  { to: '/regressions', label: 'Regressions' },
  { to: '/flaky', label: 'Flaky Tests' },
  { to: '/notifications', label: 'Notifications' },
]

export default function NavBar() {
  return (
    <nav className="bg-brand-600 text-white shadow-md">
      <div className="max-w-7xl mx-auto px-4 flex items-center h-14 gap-6">
        <span className="font-bold text-lg tracking-tight">FlowGuard</span>
        <div className="flex gap-1">
          {links.map(({ to, label }) => (
            <NavLink
              key={to}
              to={to}
              end={to === '/'}
              className={({ isActive }) =>
                `px-3 py-1 rounded text-sm font-medium transition-colors ${
                  isActive ? 'bg-white/20' : 'hover:bg-white/10'
                }`
              }
            >
              {label}
            </NavLink>
          ))}
        </div>
      </div>
    </nav>
  )
}
