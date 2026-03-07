import { useRouter } from "expo-router";
import React, { useEffect, useState } from "react";
import { Pressable, ScrollView, StyleSheet, Text, View, Animated } from "react-native";
import TimePickerModal from "../components/TimePickerModal";
import { Habit } from "../types";
import {
    debugScheduledNotifications,
    scheduleAllNotifications,
} from "../utils/notifications";
import { getHabits, saveHabits } from "../utils/storage";
import { useTheme } from "../contexts/ThemeContext";

export default function ConfigureNotificationsScreen() {
    const router = useRouter();
    const { theme } = useTheme();
    const [habits, setHabits] = useState<Habit[]>([]);
    const [currentHabitIndex, setCurrentHabitIndex] = useState(0);
    const [showTimePicker, setShowTimePicker] = useState(false);
    const [selectedTimeIndex, setSelectedTimeIndex] = useState<number | null>(null);
    const [fadeAnim] = useState(new Animated.Value(0));

    useEffect(() => {
        loadHabits();
        Animated.timing(fadeAnim, {
            toValue: 1,
            duration: 300,
            useNativeDriver: true,
        }).start();
    }, []);

    const loadHabits = async () => {
        const loadedHabits = await getHabits();
        setHabits(loadedHabits);
    };

    const currentHabit = habits[currentHabitIndex];

    const handleAddTime = () => {
        const newTime = "9:00 AM";
        const updatedHabits = [...habits];
        updatedHabits[currentHabitIndex].notificationTimes.push(newTime);
        setHabits(updatedHabits);
    };

    const handleTimeChange = (timeString: string) => {
        if (selectedTimeIndex !== null) {
            const updatedHabits = [...habits];
            updatedHabits[currentHabitIndex].notificationTimes[selectedTimeIndex] = timeString;
            setHabits(updatedHabits);
        }
        setShowTimePicker(false);
        setSelectedTimeIndex(null);
    };

    const handleRemoveTime = (index: number) => {
        if (currentHabit.notificationTimes.length <= 1) return;

        const updatedHabits = [...habits];
        updatedHabits[currentHabitIndex].notificationTimes.splice(index, 1);
        setHabits(updatedHabits);
    };

    const openTimePicker = (index: number) => {
        setSelectedTimeIndex(index);
        setShowTimePicker(true);
    };

    const handleNext = async () => {
        if (currentHabitIndex < habits.length - 1) {
            setCurrentHabitIndex(currentHabitIndex + 1);
        } else {
            await saveHabits(habits);
            await scheduleAllNotifications(habits);
            await debugScheduledNotifications();
            router.replace("/home");
        }
    };

    const handleSkip = async () => {
        await saveHabits(habits);
        await scheduleAllNotifications(habits);
        await debugScheduledNotifications();
        router.replace("/home");
    };

    if (!currentHabit) return null;

    const dynamicStyles = StyleSheet.create({
        container: {
            flex: 1,
            backgroundColor: theme.colors.background,
        },
        header: {
            backgroundColor: theme.colors.primary,
            paddingTop: 60,
            paddingBottom: theme.spacing.xl,
            paddingHorizontal: theme.spacing.lg,
            borderBottomLeftRadius: theme.radius.xl,
            borderBottomRightRadius: theme.radius.xl,
        },
        headerContent: {
            alignItems: "center",
        },
        headerTitle: {
            fontSize: theme.typography.fontSize['3xl'],
            fontWeight: "800",
            color: theme.colors.textInverse,
            textAlign: "center",
            marginBottom: 4,
        },
        headerSubtitle: {
            fontSize: theme.typography.fontSize.base,
            color: theme.colors.textInverse,
            opacity: 0.9,
            fontWeight: "600",
            textAlign: "center",
        },
        content: {
            padding: theme.spacing.lg,
        },
        habitInfo: {
            flexDirection: "row",
            alignItems: "center",
            backgroundColor: theme.colors.card,
            padding: theme.spacing.lg,
            borderRadius: theme.radius.lg,
            marginBottom: theme.spacing.xl,
            ...theme.shadows.md,
            borderWidth: 1,
            borderColor: theme.colors.border,
        },
        habitIconBadge: {
            width: 64,
            height: 64,
            borderRadius: theme.radius.lg,
            backgroundColor: theme.colors.successLight,
            alignItems: "center",
            justifyContent: "center",
            marginRight: theme.spacing.md,
        },
        habitIconSmall: {
            fontSize: 32,
        },
        taskLabel: {
            fontSize: theme.typography.fontSize.xs,
            color: theme.colors.textTertiary,
            fontWeight: "700",
            marginBottom: 4,
            letterSpacing: 1,
        },
        habitName: {
            fontSize: theme.typography.fontSize.xl,
            fontWeight: "800",
            color: theme.colors.text,
        },
        timesContainer: {
            gap: theme.spacing.md,
            marginBottom: theme.spacing.lg,
        },
        timeRow: {
            flexDirection: "row",
            alignItems: "center",
            gap: theme.spacing.md,
        },
        timePickerButton: {
            flex: 1,
            backgroundColor: theme.colors.card,
            padding: theme.spacing.lg,
            borderRadius: theme.radius.md,
            borderWidth: 2,
            borderColor: theme.colors.border,
            ...theme.shadows.sm,
        },
        timeText: {
            fontSize: theme.typography.fontSize['3xl'],
            fontWeight: "800",
            color: theme.colors.text,
            textAlign: "center",
        },
        removeButton: {
            width: 48,
            height: 48,
            borderRadius: theme.radius.md,
            backgroundColor: theme.colors.danger,
            alignItems: "center",
            justifyContent: "center",
        },
        removeText: {
            color: theme.colors.textInverse,
            fontSize: 24,
            fontWeight: "700",
        },
        addTimeButton: {
            backgroundColor: theme.colors.card,
            padding: theme.spacing.md,
            borderRadius: theme.radius.md,
            borderWidth: 2,
            borderColor: theme.colors.primary,
            borderStyle: "dashed",
            alignItems: "center",
        },
        addTimeText: {
            color: theme.colors.primary,
            fontSize: theme.typography.fontSize.base,
            fontWeight: "700",
        },
        progressIndicator: {
            alignItems: "center",
            marginTop: theme.spacing.xl,
            padding: theme.spacing.md,
            backgroundColor: theme.colors.surface,
            borderRadius: theme.radius.md,
        },
        progressText: {
            fontSize: theme.typography.fontSize.sm,
            color: theme.colors.textSecondary,
            fontWeight: "600",
        },
        footer: {
            flexDirection: "row",
            padding: theme.spacing.lg,
            backgroundColor: theme.colors.elevated,
            borderTopWidth: 1,
            borderTopColor: theme.colors.border,
            gap: theme.spacing.md,
        },
        skipButton: {
            flex: 1,
            paddingVertical: theme.spacing.md,
            borderRadius: theme.radius.md,
            backgroundColor: theme.colors.surface,
            alignItems: "center",
            borderWidth: 2,
            borderColor: theme.colors.border,
        },
        skipText: {
            color: theme.colors.textSecondary,
            fontSize: theme.typography.fontSize.base,
            fontWeight: "700",
        },
        nextButton: {
            flex: 1,
            paddingVertical: theme.spacing.md,
            borderRadius: theme.radius.md,
            backgroundColor: theme.colors.primary,
            alignItems: "center",
            ...theme.shadows.md,
        },
        nextText: {
            color: theme.colors.textInverse,
            fontSize: theme.typography.fontSize.base,
            fontWeight: "700",
        },
    });

    return (
        <View style={dynamicStyles.container}>
            <View style={dynamicStyles.header}>
                <View style={dynamicStyles.headerContent}>
                    <Text style={dynamicStyles.headerTitle}>Notifications</Text>
                    <Text style={dynamicStyles.headerSubtitle}>Set your reminder times</Text>
                </View>
            </View>

            <Animated.View style={{ flex: 1, opacity: fadeAnim }}>
                <ScrollView contentContainerStyle={dynamicStyles.content}>
                    <View style={dynamicStyles.habitInfo}>
                        <View style={dynamicStyles.habitIconBadge}>
                            <Text style={dynamicStyles.habitIconSmall}>{currentHabit.icon}</Text>
                        </View>
                        <View style={{ flex: 1 }}>
                            <Text style={dynamicStyles.taskLabel}>HABIT</Text>
                            <Text style={dynamicStyles.habitName} numberOfLines={2}>
                                {currentHabit.name}
                            </Text>
                        </View>
                    </View>

                    <View style={dynamicStyles.timesContainer}>
                        {currentHabit.notificationTimes.map((time, index) => (
                            <View key={index} style={dynamicStyles.timeRow}>
                                <Pressable
                                    style={dynamicStyles.timePickerButton}
                                    onPress={() => openTimePicker(index)}
                                    accessibilityLabel={`Notification time ${time}`}
                                    accessibilityRole="button"
                                >
                                    <Text style={dynamicStyles.timeText}>{time}</Text>
                                </Pressable>
                                {currentHabit.notificationTimes.length > 1 && (
                                    <Pressable
                                        onPress={() => handleRemoveTime(index)}
                                        style={dynamicStyles.removeButton}
                                        accessibilityLabel="Remove time"
                                        accessibilityRole="button"
                                    >
                                        <Text style={dynamicStyles.removeText}>×</Text>
                                    </Pressable>
                                )}
                            </View>
                        ))}
                    </View>

                    <Pressable 
                        style={dynamicStyles.addTimeButton} 
                        onPress={handleAddTime}
                        accessibilityLabel="Add another time"
                        accessibilityRole="button"
                    >
                        <Text style={dynamicStyles.addTimeText}>+ Add Another Time</Text>
                    </Pressable>

                    <View style={dynamicStyles.progressIndicator}>
                        <Text style={dynamicStyles.progressText}>
                            Habit {currentHabitIndex + 1} of {habits.length}
                        </Text>
                    </View>
                </ScrollView>
            </Animated.View>

            <View style={dynamicStyles.footer}>
                <Pressable 
                    style={dynamicStyles.skipButton} 
                    onPress={handleSkip}
                    accessibilityLabel="Skip setup"
                    accessibilityRole="button"
                >
                    <Text style={dynamicStyles.skipText}>Skip Setup</Text>
                </Pressable>
                <Pressable 
                    style={dynamicStyles.nextButton} 
                    onPress={handleNext}
                    accessibilityLabel={currentHabitIndex < habits.length - 1 ? "Next habit" : "Finish setup"}
                    accessibilityRole="button"
                >
                    <Text style={dynamicStyles.nextText}>
                        {currentHabitIndex < habits.length - 1 ? "Next" : "Finish"}
                    </Text>
                </Pressable>
            </View>

            {selectedTimeIndex !== null && (
                <TimePickerModal
                    visible={showTimePicker}
                    initialTime={currentHabit.notificationTimes[selectedTimeIndex]}
                    onConfirm={handleTimeChange}
                    onCancel={() => {
                        setShowTimePicker(false);
                        setSelectedTimeIndex(null);
                    }}
                />
            )}
        </View>
    );
}