import { Habit } from "../types";
import { getDailyStatus, getTodayDate } from "./storage";

export const calculateStreak = async (habit: Habit): Promise<number> => {
    const dailyStatus = await getDailyStatus();
    const habitStatus = dailyStatus[habit.id] || {};

    let streak = 0;
    let currentDate = new Date();

    while (true) {
        const dateString = currentDate.toISOString().split("T")[0];
        const status = habitStatus[dateString];

        if (status === "done") {
            streak++;
        } else if (status === "skipped") {
        } else {
            const today = getTodayDate();
            if (dateString === today) {
            } else {
                break;
            }
        }

        currentDate.setDate(currentDate.getDate() - 1);
        if (streak > 365) break;
    }

    return streak;
};

export const recalculateAllStreaks = async (
    habits: Habit[],
): Promise<Habit[]> => {
    const updatedHabits = [];

    for (const habit of habits) {
        const newStreak = await calculateStreak(habit);
        updatedHabits.push({
            ...habit,
            streak: newStreak,
        });
    }

    return updatedHabits;
};

export const willStreakBreak = async (habit: Habit): Promise<boolean> => {
    const today = getTodayDate();
    const dailyStatus = await getDailyStatus();
    const todayStatus = dailyStatus[habit.id]?.[today];

    return todayStatus !== "done" && todayStatus !== "skipped";
};

export const getLongestStreak = async (habit: Habit): Promise<number> => {
    const dailyStatus = await getDailyStatus();
    const habitStatus = dailyStatus[habit.id] || {};

    const dates = Object.keys(habitStatus).sort();
    let longestStreak = 0;
    let currentStreak = 0;

    for (let i = 0; i < dates.length; i++) {
        const status = habitStatus[dates[i]];

        if (status === "done") {
            currentStreak++;
            longestStreak = Math.max(longestStreak, currentStreak);
        } else if (status === "skipped") {
            continue;
        } else {
            currentStreak = 0;
        }
    }

    return longestStreak;
};
