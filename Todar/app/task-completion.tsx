import { useLocalSearchParams, useRouter } from "expo-router";
import React, { useEffect, useState } from "react";
import { Pressable, StyleSheet, Text, View, Animated } from "react-native";
import { Habit } from "../types";
import {
    getHabits,
    getTaskStatus,
    getTodayDate,
    saveHabits,
    updateTaskStatus,
} from "../utils/storage";
import { calculateStreak } from "../utils/streaks";
import { useTheme } from "../contexts/ThemeContext";
import CustomDialog from "../components/CustomDialog";

export default function TaskCompletionScreen() {
    const router = useRouter();
    const { theme } = useTheme();
    const { habitId } = useLocalSearchParams();
    const [habit, setHabit] = useState<Habit | null>(null);
    const [loading, setLoading] = useState(true);
    const [scaleAnim] = useState(new Animated.Value(0.9));
    const [fadeAnim] = useState(new Animated.Value(0));
    const [dialogVisible, setDialogVisible] = useState(false);
    const [dialogConfig, setDialogConfig] = useState({
        title: '',
        message: '',
    });

    useEffect(() => {
        loadHabit();
        Animated.parallel([
            Animated.spring(scaleAnim, {
                toValue: 1,
                tension: 50,
                friction: 7,
                useNativeDriver: true,
            }),
            Animated.timing(fadeAnim, {
                toValue: 1,
                duration: 300,
                useNativeDriver: true,
            }),
        ]).start();
    }, []);

    const loadHabit = async () => {
        try {
            const habits = await getHabits();
            const foundHabit = habits.find((h) => h.id === habitId);
            if (foundHabit) {
                setHabit(foundHabit);
            }
        } catch (error) {
            console.error('Failed to load habit:', error);
        } finally {
            setLoading(false);
        }
    };

    const updateStreak = async (status: "done" | "skipped") => {
        if (!habit) return;

        try {
            const habits = await getHabits();
            const habitIndex = habits.findIndex((h) => h.id === habit.id);
            if (habitIndex === -1) return;

            const today = getTodayDate();

            if (status === "done") {
                const todayStatus = await getTaskStatus(habit.id, today);
                if (todayStatus === "done") {
                    setDialogConfig({
                        title: "Already Done!",
                        message: "You've already completed this habit today. Keep up the great work!",
                    });
                    setDialogVisible(true);
                    return;
                }

                await updateTaskStatus(habit.id, today, "done");
                const newStreak = await calculateStreak(habits[habitIndex]);
                habits[habitIndex].streak = newStreak;
            } else if (status === "skipped") {
                await updateTaskStatus(habit.id, today, "skipped");
                const newStreak = await calculateStreak(habits[habitIndex]);
                habits[habitIndex].streak = newStreak;
            }

            await saveHabits(habits);
            router.back();
        } catch (error) {
            console.error("Failed to update streak:", error);
            setDialogConfig({
                title: "Error",
                message: "Failed to update your progress. Please try again.",
            });
            setDialogVisible(true);
        }
    };

    const dynamicStyles = StyleSheet.create({
        container: {
            flex: 1,
            backgroundColor: theme.colors.background,
            justifyContent: "center",
            alignItems: "center",
            padding: theme.spacing.lg,
        },
        content: {
            width: "100%",
            maxWidth: 400,
            alignItems: "center",
        },
        iconContainer: {
            width: 120,
            height: 120,
            borderRadius: theme.radius.full,
            backgroundColor: theme.colors.primaryLight + '30',
            alignItems: "center",
            justifyContent: "center",
            marginBottom: theme.spacing.xl,
            ...theme.shadows.lg,
        },
        habitIcon: {
            fontSize: 60,
        },
        habitName: {
            fontSize: theme.typography.fontSize['3xl'],
            fontWeight: "800",
            color: theme.colors.text,
            marginBottom: theme.spacing.md,
            textAlign: "center",
        },
        question: {
            fontSize: theme.typography.fontSize.lg,
            color: theme.colors.textSecondary,
            marginBottom: theme.spacing['2xl'],
            textAlign: "center",
            fontWeight: "600",
        },
        buttonContainer: {
            width: "100%",
            gap: theme.spacing.md,
        },
        doneButton: {
            backgroundColor: theme.colors.success,
            paddingVertical: theme.spacing.lg,
            borderRadius: theme.radius.lg,
            flexDirection: "row",
            alignItems: "center",
            justifyContent: "center",
            gap: theme.spacing.sm,
            ...theme.shadows.md,
        },
        doneIcon: {
            fontSize: 28,
            color: theme.colors.textInverse,
        },
        doneText: {
            color: theme.colors.textInverse,
            fontSize: theme.typography.fontSize.xl,
            fontWeight: "800",
            letterSpacing: 1,
        },
        skipButton: {
            backgroundColor: theme.colors.warning,
            paddingVertical: theme.spacing.lg,
            borderRadius: theme.radius.lg,
            flexDirection: "row",
            alignItems: "center",
            justifyContent: "center",
            gap: theme.spacing.sm,
            ...theme.shadows.md,
        },
        skipIcon: {
            fontSize: 28,
            color: theme.colors.textInverse,
        },
        skipText: {
            color: theme.colors.textInverse,
            fontSize: theme.typography.fontSize.xl,
            fontWeight: "800",
            letterSpacing: 1,
        },
        backButton: {
            marginTop: theme.spacing.xl,
            padding: theme.spacing.md,
        },
        backButtonText: {
            color: theme.colors.textSecondary,
            fontSize: theme.typography.fontSize.base,
            fontWeight: "600",
        },
    });

    if (loading || !habit) {
        return (
            <View style={dynamicStyles.container}>
                <Text style={dynamicStyles.question}>Loading...</Text>
            </View>
        );
    }

    return (
        <Animated.View 
            style={[
                dynamicStyles.container,
                { opacity: fadeAnim, transform: [{ scale: scaleAnim }] }
            ]}
        >
            <View style={dynamicStyles.content}>
                <View style={dynamicStyles.iconContainer}>
                    <Text style={dynamicStyles.habitIcon}>{habit.icon}</Text>
                </View>
                
                <Text style={dynamicStyles.habitName}>{habit.name}</Text>
                <Text style={dynamicStyles.question}>
                    Did you complete this habit?
                </Text>

                <View style={dynamicStyles.buttonContainer}>
                    <Pressable 
                        style={dynamicStyles.doneButton} 
                        onPress={() => updateStreak("done")}
                        accessibilityLabel="Mark as done"
                        accessibilityRole="button"
                    >
                        <Text style={dynamicStyles.doneIcon}>✓</Text>
                        <Text style={dynamicStyles.doneText}>DONE</Text>
                    </Pressable>

                    <Pressable 
                        style={dynamicStyles.skipButton} 
                        onPress={() => updateStreak("skipped")}
                        accessibilityLabel="Skip for today"
                        accessibilityRole="button"
                    >
                        <Text style={dynamicStyles.skipIcon}>→</Text>
                        <Text style={dynamicStyles.skipText}>SKIP</Text>
                    </Pressable>
                </View>
            </View>

            <CustomDialog
                visible={dialogVisible}
                title={dialogConfig.title}
                message={dialogConfig.message}
                buttons={[
                    {
                        text: "OK",
                        onPress: () => setDialogVisible(false),
                    },
                ]}
                onClose={() => setDialogVisible(false)}
            />
        </Animated.View>
    );
}