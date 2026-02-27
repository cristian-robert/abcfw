"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { cn } from "@/lib/utils";
import { Database, FileJson } from "lucide-react";

const navItems = [
  { href: "/browser", label: "Browser", icon: Database },
  { href: "/templates", label: "Templates", icon: FileJson },
];

export function NavBar() {
  const pathname = usePathname();

  return (
    <nav className="flex items-center gap-1 px-4 h-12 border-b border-white/[0.06] bg-[#0a0a0b] shrink-0">
      <span className="text-sm font-semibold text-foreground mr-4">
        Kafka Tools
      </span>
      {navItems.map(({ href, label, icon: Icon }) => {
        const isActive = pathname.startsWith(href);
        return (
          <Link
            key={href}
            href={href}
            className={cn(
              "flex items-center gap-1.5 px-3 py-1.5 rounded-md text-sm transition-colors",
              isActive
                ? "bg-white/[0.08] text-teal-400"
                : "text-muted-foreground hover:text-foreground hover:bg-white/[0.04]"
            )}
          >
            <Icon className="h-4 w-4" />
            {label}
          </Link>
        );
      })}
    </nav>
  );
}
