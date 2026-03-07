import { useRouter } from "expo-router";
import React, { useState } from "react";
import {
    Pressable,
    ScrollView,
    StyleSheet,
    Text,
    View,
    Animated,
} from "react-native";
import { HABIT_TEMPLATES, Habit } from "../types";
import { getTodayDate, saveHabits } from "../utils/storage";
import { useTheme } from "../contexts/ThemeContext";
import CustomDialog from "../components/CustomDialog";

export default function SelectHabitsScreen() {
    const router = useRouter();
    const { theme } = useTheme();
    const [selectedHabits, setSelectedHabits] = useState<string[]>([]);
    const [fadeAnim] = useState(new Animated.Value(0));
    const [dialogVisible, setDialogVisible] = useState(false);

    React.useEffect(() => {
        Animated.timing(fadeAnim, {
            toValue: 1,
            duration: 300,
            useNativeDriver: true,
        }).start();
    }, []);

    const toggleHabit = (habitName: string) => {
        if (selectedHabits.includes(habitName)) {
            setSelectedHabits(selectedHabits.filter(habit => habit !== habitName));
        } else {
            setSelectedHabits([habitName]);
        }
    };

    const handleContinue = async () => {
        if (selectedHabits.length === 0) {
            setDialogVisible(true);
            return;
        }

        const habits: Habit[] = selectedHabits.map((name) => {
            const template = HABIT_TEMPLATES.find((t) => t.name === name);
            return {
                id: Date.now().toString() + Math.random(),
                name,
                icon: template?.icon || "🎯",
                notificationTimes: template?.defaultTimes || ["9:00 AM"],
                streak: 0,
                lastCompleted: null,
                createdAt: getTodayDate(),
            };
        });

        await saveHabits(habits);
        router.push("/configure-notifications");
    };

    const handleSkipToStreaks = async () => {
        await saveHabits([]);
        router.push("/home");
    };

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
            marginBottom: theme.spacing.xs,
        },
        headerSubtitle: {
            fontSize: theme.typography.fontSize.base,
            color: theme.colors.textInverse,
            opacity: 0.9,
            fontWeight: "600",
            textAlign: "center",
            lineHeight: 22,
        },
        scrollContent: {
            padding: theme.spacing.lg,
        },
        grid: {
            flexDirection: "row",
            flexWrap: "wrap",
            gap: theme.spacing.md,
            justifyContent: "space-between",
        },
        habitCard: {
            width: "47%",
            backgroundColor: theme.colors.card,
            borderRadius: theme.radius.xl,
            padding: theme.spacing.lg,
            alignItems: "center",
            borderWidth: 2,
            borderColor: theme.colors.border,
            ...theme.shadows.sm,
        },
        habitCardSelected: {
            borderColor: theme.colors.primary,
            backgroundColor: theme.colors.primaryLight + '20',
            transform: [{ scale: 1.02 }],
            ...theme.shadows.md,
        },
        habitIconContainer: {
            width: 80,
            height: 80,
            backgroundColor: theme.colors.surface,
            borderRadius: theme.radius.lg,
            alignItems: "center",
            justifyContent: "center",
            marginBottom: theme.spacing.md,
        },
        habitIconContainerSelected: {
            backgroundColor: theme.colors.primary,
        },
        habitIcon: {
            fontSize: 40,
            textAlign: "center",
            lineHeight: 40,
        },
        habitName: {
            fontSize: theme.typography.fontSize.base,
            fontWeight: "700",
            color: theme.colors.text,
            textAlign: "center",
        },
        habitNameSelected: {
            color: theme.colors.primary,
        },
        selectedBadge: {
            backgroundColor: theme.colors.primary,
            paddingHorizontal: theme.spacing.sm,
            paddingVertical: 4,
            borderRadius: theme.radius.full,
            marginTop: theme.spacing.xs,
        },
        selectedBadgeText: {
            fontSize: theme.typography.fontSize.xs,
            color: theme.colors.textInverse,
            fontWeight: "700",
        },
        footer: {
            padding: theme.spacing.lg,
            backgroundColor: theme.colors.elevated,
            borderTopWidth: 1,
            borderTopColor: theme.colors.border,
            flexDirection: "row",
            gap: theme.spacing.md,
        },
        skipButton: {
            flex: 1,
            paddingVertical: theme.spacing.md,
            borderRadius: theme.radius.xl,
            alignItems: "center",
            backgroundColor: theme.colors.surface,
            borderWidth: 2,
            borderColor: theme.colors.border,
        },
        skipText: {
            color: theme.colors.textSecondary,
            fontSize: theme.typography.fontSize.base,
            fontWeight: "700",
        },
        continueButton: {
            flex: 1,
            backgroundColor: theme.colors.primary,
            paddingVertical: theme.spacing.md,
            paddingHorizontal: theme.spacing.sm,
            borderRadius: theme.radius.xl,
            alignItems: "center",
            justifyContent: "center",
            ...theme.shadows.md,
        },
        continueText: {
            color: theme.colors.textInverse,
            fontSize: theme.typography.fontSize.base,
            fontWeight: "700",
            textAlign: "center",
        },
    });

    return (
        <View style={dynamicStyles.container}>
            <View style={dynamicStyles.header}>
                <View style={dynamicStyles.headerContent}>
                    <Text style={dynamicStyles.headerTitle}>
                        Choose Your Daily{"\n"}Reminder
                    </Text>
                    <Text style={dynamicStyles.headerSubtitle}>
                        Select one habit to get started.{"\n"}Tap another to switch selection.
                    </Text>
                </View>
            </View>

            <Animated.View style={{ flex: 1, opacity: fadeAnim }}>
                <ScrollView contentContainerStyle={dynamicStyles.scrollContent}>
                    <View style={dynamicStyles.grid}>
                        {HABIT_TEMPLATES.map((template) => {
                            const isSelected = selectedHabits.includes(template.name);
                            
                            return (
                                <Pressable
                                    key={template.name}
                                    style={[
                                        dynamicStyles.habitCard,
                                        isSelected && dynamicStyles.habitCardSelected,
                                    ]}
                                    onPress={() => toggleHabit(template.name)}
                                    accessibilityLabel={`${template.name} habit`}
                                    accessibilityRole="button"
                                    accessibilityState={{ selected: isSelected }}
                                >
                                    <View style={[
                                        dynamicStyles.habitIconContainer,
                                        isSelected && dynamicStyles.habitIconContainerSelected,
                                    ]}>
                                        <Text style={dynamicStyles.habitIcon}>
                                            {template.icon}
                                        </Text>
                                    </View>
                                    <Text style={[
                                        dynamicStyles.habitName,
                                        isSelected && dynamicStyles.habitNameSelected,
                                    ]}>
                                        {template.name}
                                    </Text>
                                    {isSelected && (
                                        <View style={dynamicStyles.selectedBadge}>
                                            <Text style={dynamicStyles.selectedBadgeText}>✓ Selected</Text>
                                        </View>
                                    )}
                                </Pressable>
                            );
                        })}
                    </View>
                </ScrollView>
            </Animated.View>

            <View style={dynamicStyles.footer}>
                <Pressable
                    style={dynamicStyles.skipButton}
                    onPress={handleSkipToStreaks}
                    accessibilityLabel="Skip to home"
                    accessibilityRole="button"
                >
                    <Text style={dynamicStyles.skipText}>Skip</Text>
                </Pressable>
                {selectedHabits.length > 0 && (
                    <Pressable
                        style={dynamicStyles.continueButton}
                        onPress={handleContinue}
                        accessibilityLabel="Continue"
                        accessibilityRole="button"
                    >
                        <Text style={dynamicStyles.continueText} numberOfLines={1} ellipsizeMode="tail">
                            Continue with {selectedHabits[0].length > 12 ? selectedHabits[0].substring(0, 12) + "..." : selectedHabits[0]}
                        </Text>
                    </Pressable>
                )}
            </View>

            <CustomDialog
                visible={dialogVisible}
                title="No Habit Selected"
                message="Please select at least one habit to continue, or tap Skip to go to the home screen."
                buttons={[
                    {
                        text: "OK",
                        onPress: () => setDialogVisible(false),
                    },
                ]}
                onClose={() => setDialogVisible(false)}
            />
        </View>
    );
}