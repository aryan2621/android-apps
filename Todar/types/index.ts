export interface Habit {
    id: string;
    name: string;
    icon: string; // emoji
    notificationTimes: string[]; // ['08:30', '14:00']
    streak: number;
    lastCompleted: string | null; // 'YYYY-MM-DD'
    createdAt: string; // 'YYYY-MM-DD'
}

export type TaskStatus = "done" | "skipped" | "pending";

export interface DailyStatus {
    [habitId: string]: {
        [date: string]: TaskStatus; // '2025-10-02': 'done'
    };
}

export interface HabitTemplate {
    name: string;
    icon: string;
    defaultTimes: string[];
}

export const HABIT_TEMPLATES: HabitTemplate[] = [
    { name: "Gym", icon: "🏋️", defaultTimes: ["7:00 AM"] },
    { name: "Learning", icon: "📚", defaultTimes: ["8:00 PM"] },
    {
        name: "Drink Water",
        icon: "💧",
        defaultTimes: ["9:00 AM", "12:00 PM", "3:00 PM", "6:00 PM"],
    },
    { name: "Meditate", icon: "🧘", defaultTimes: ["6:00 AM", "9:00 PM"] },
    { name: "Read", icon: "📖", defaultTimes: ["10:00 PM"] },
    { name: "Journal", icon: "✍️", defaultTimes: ["9:00 PM"] },
];
