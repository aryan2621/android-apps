import { useRouter } from "expo-router";
import React, { useState, useEffect } from "react";
import {
    Pressable,
    ScrollView,
    StyleSheet,
    Text,
    View,
    Switch,
    Animated,
} from "react-native";
import { getHabits, clearAllData, saveHabits } from "../utils/storage";
import { Habit } from "../types";
import { useTheme } from "../contexts/ThemeContext";
import CustomDialog from "../components/CustomDialog";
import BottomNavigation from "../components/BottomNavigation";

export default function SettingsScreen() {
    const router = useRouter();
    const { theme, toggleTheme, isDark } = useTheme();
    const [habits, setHabits] = useState<Habit[]>([]);
    const [notificationsEnabled, setNotificationsEnabled] = useState(true);
    const [dialogVisible, setDialogVisible] = useState(false);
    const [dialogConfig, setDialogConfig] = useState({
        title: '',
        message: '',
        buttons: [] as Array<{ text: string; onPress: () => void; style?: 'default' | 'cancel' | 'destructive' }>,
    });
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

    const showDialog = (title: string, message: string, buttons: Array<{ text: string; onPress: () => void; style?: 'default' | 'cancel' | 'destructive' }>) => {
        setDialogConfig({ title, message, buttons });
        setDialogVisible(true);
    };

    const handleResetAllStreaks = () => {
        showDialog(
            "Reset All Streaks",
            "This will reset all your habit streaks to 0. This action cannot be undone.",
            [
                {
                    text: "Cancel",
                    style: "cancel",
                    onPress: () => {},
                },
                {
                    text: "Reset",
                    style: "destructive",
                    onPress: async () => {
                        const resetHabits = habits.map(habit => ({
                            ...habit,
                            streak: 0,
                            lastCompleted: null,
                        }));
                        await saveHabits(resetHabits);
                        await loadHabits();
                        showDialog("Success", "All streaks have been reset to 0.", [
                            { text: "OK", onPress: () => {} }
                        ]);
                    },
                },
            ]
        );
    };

    const handleDeleteAllData = () => {
        showDialog(
            "Delete All Data",
            "This will permanently delete all your habits, streaks, and progress. This action cannot be undone.",
            [
                {
                    text: "Cancel",
                    style: "cancel",
                    onPress: () => {},
                },
                {
                    text: "Delete",
                    style: "destructive",
                    onPress: async () => {
                        await clearAllData();
                        await loadHabits();
                        showDialog("Success", "All data has been deleted.", [
                            { text: "OK", onPress: () => router.replace("/") }
                        ]);
                    },
                },
            ]
        );
    };

    const handleExportData = () => {
        const totalStreaks = habits.reduce((sum, habit) => sum + habit.streak, 0);
        
        showDialog(
            "Export Data",
            `Your data summary:\n\n• ${habits.length} habits\n• ${totalStreaks} total streak days\n\nExport functionality would be implemented here.`,
            [{ text: "OK", onPress: () => {} }]
        );
    };

    const handleResetSingleStreak = (habitId: string, habitName: string) => {
        showDialog(
            "Reset Streak",
            `Reset the streak for "${habitName}" to 0?`,
            [
                {
                    text: "Cancel",
                    style: "cancel",
                    onPress: () => {},
                },
                {
                    text: "Reset",
                    style: "destructive",
                    onPress: async () => {
                        const updatedHabits = habits.map(habit => 
                            habit.id === habitId 
                                ? { ...habit, streak: 0, lastCompleted: null }
                                : habit
                        );
                        await saveHabits(updatedHabits);
                        await loadHabits();
                        showDialog("Success", `Streak for "${habitName}" has been reset.`, [
                            { text: "OK", onPress: () => {} }
                        ]);
                    },
                },
            ]
        );
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
            marginBottom: 4,
            textAlign: "center",
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
            flexGrow: 1,
        },
        section: {
            marginBottom: theme.spacing.xl,
        },
        sectionTitle: {
            fontSize: theme.typography.fontSize.lg,
            fontWeight: "700",
            color: theme.colors.text,
            marginBottom: theme.spacing.md,
        },
        settingItem: {
            flexDirection: "row",
            alignItems: "center",
            justifyContent: "space-between",
            backgroundColor: theme.colors.card,
            padding: theme.spacing.md,
            borderRadius: theme.radius.md,
            marginBottom: theme.spacing.sm,
            borderWidth: 1,
            borderColor: theme.colors.border,
            ...theme.shadows.sm,
        },
        settingInfo: {
            flex: 1,
            marginRight: theme.spacing.md,
        },
        settingLabel: {
            fontSize: theme.typography.fontSize.base,
            fontWeight: "700",
            color: theme.colors.text,
            marginBottom: 4,
        },
        settingDescription: {
            fontSize: theme.typography.fontSize.sm,
            color: theme.colors.textSecondary,
        },
        primaryButton: {
            backgroundColor: theme.colors.primary,
            paddingHorizontal: theme.spacing.md,
            paddingVertical: theme.spacing.sm,
            borderRadius: theme.radius.sm,
        },
        primaryButtonText: {
            color: theme.colors.textInverse,
            fontSize: theme.typography.fontSize.sm,
            fontWeight: "700",
        },
        dangerButton: {
            backgroundColor: theme.colors.danger,
            paddingHorizontal: theme.spacing.md,
            paddingVertical: theme.spacing.sm,
            borderRadius: theme.radius.sm,
        },
        dangerButtonText: {
            color: theme.colors.textInverse,
            fontSize: theme.typography.fontSize.sm,
            fontWeight: "700",
        },
        habitItem: {
            flexDirection: "row",
            alignItems: "center",
            justifyContent: "space-between",
            backgroundColor: theme.colors.card,
            padding: theme.spacing.md,
            borderRadius: theme.radius.md,
            marginBottom: theme.spacing.sm,
            borderWidth: 1,
            borderColor: theme.colors.border,
            ...theme.shadows.sm,
        },
        habitInfo: {
            flexDirection: "row",
            alignItems: "center",
            flex: 1,
        },
        habitIcon: {
            fontSize: 28,
            marginRight: theme.spacing.md,
        },
        habitDetails: {
            flex: 1,
        },
        habitName: {
            fontSize: theme.typography.fontSize.base,
            fontWeight: "700",
            color: theme.colors.text,
            marginBottom: 4,
        },
        habitStreak: {
            fontSize: theme.typography.fontSize.sm,
            color: theme.colors.textSecondary,
        },
        resetButton: {
            backgroundColor: theme.colors.warning,
            paddingHorizontal: theme.spacing.sm,
            paddingVertical: theme.spacing.xs,
            borderRadius: theme.radius.sm,
        },
        resetButtonText: {
            color: theme.colors.textInverse,
            fontSize: theme.typography.fontSize.xs,
            fontWeight: "700",
        },
    });

    return (
        <View style={dynamicStyles.container}>
            <View style={dynamicStyles.header}>
                <View style={dynamicStyles.headerContent}>
                    <Text style={dynamicStyles.headerTitle}>Settings</Text>
                    <Text style={dynamicStyles.headerSubtitle}>Manage your habits</Text>
                </View>
            </View>

            <Animated.View style={{ flex: 1, opacity: fadeAnim }}>
                <ScrollView contentContainerStyle={dynamicStyles.content}>
                    {/* App Settings Section */}
                    <View style={dynamicStyles.section}>
                        <Text style={dynamicStyles.sectionTitle}>App Settings</Text>
                        
                        <View style={dynamicStyles.settingItem}>
                            <View style={dynamicStyles.settingInfo}>
                                <Text style={dynamicStyles.settingLabel}>Dark Mode</Text>
                                <Text style={dynamicStyles.settingDescription}>
                                    Switch to dark theme
                                </Text>
                            </View>
                            <Switch
                                value={isDark}
                                onValueChange={toggleTheme}
                                trackColor={{ false: theme.colors.border, true: theme.colors.primary }}
                                thumbColor={theme.colors.card}
                            />
                        </View>

                        <View style={dynamicStyles.settingItem}>
                            <View style={dynamicStyles.settingInfo}>
                                <Text style={dynamicStyles.settingLabel}>Notifications</Text>
                                <Text style={dynamicStyles.settingDescription}>
                                    Receive habit reminders
                                </Text>
                            </View>
                            <Switch
                                value={notificationsEnabled}
                                onValueChange={setNotificationsEnabled}
                                trackColor={{ false: theme.colors.border, true: theme.colors.primary }}
                                thumbColor={theme.colors.card}
                            />
                        </View>
                    </View>

                    {/* Streak Management Section */}
                    <View style={dynamicStyles.section}>
                        <Text style={dynamicStyles.sectionTitle}>Data Management</Text>
                        
                        <View style={dynamicStyles.settingItem}>
                            <View style={dynamicStyles.settingInfo}>
                                <Text style={dynamicStyles.settingLabel}>Export Data</Text>
                                <Text style={dynamicStyles.settingDescription}>
                                    Export your habits and progress
                                </Text>
                            </View>
                            <Pressable
                                style={dynamicStyles.primaryButton}
                                onPress={handleExportData}
                            >
                                <Text style={dynamicStyles.primaryButtonText}>Export</Text>
                            </Pressable>
                        </View>

                        <View style={dynamicStyles.settingItem}>
                            <View style={dynamicStyles.settingInfo}>
                                <Text style={dynamicStyles.settingLabel}>Reset All Streaks</Text>
                                <Text style={dynamicStyles.settingDescription}>
                                    Reset all habit streaks to 0
                                </Text>
                            </View>
                            <Pressable
                                style={dynamicStyles.dangerButton}
                                onPress={handleResetAllStreaks}
                            >
                                <Text style={dynamicStyles.dangerButtonText}>Reset</Text>
                            </Pressable>
                        </View>
                    </View>

                    {/* Individual Habit Streaks */}
                    {habits.length > 0 && (
                        <View style={dynamicStyles.section}>
                            <Text style={dynamicStyles.sectionTitle}>Individual Streaks</Text>
                            {habits.map((habit) => (
                                <View key={habit.id} style={dynamicStyles.habitItem}>
                                    <View style={dynamicStyles.habitInfo}>
                                        <Text style={dynamicStyles.habitIcon}>{habit.icon}</Text>
                                        <View style={dynamicStyles.habitDetails}>
                                            <Text style={dynamicStyles.habitName}>{habit.name}</Text>
                                            <Text style={dynamicStyles.habitStreak}>
                                                Current streak: {habit.streak} days
                                            </Text>
                                        </View>
                                    </View>
                                    <Pressable
                                        style={dynamicStyles.resetButton}
                                        onPress={() => handleResetSingleStreak(habit.id, habit.name)}
                                    >
                                        <Text style={dynamicStyles.resetButtonText}>Reset</Text>
                                    </Pressable>
                                </View>
                            ))}
                        </View>
                    )}

                    {/* Danger Zone */}
                    <View style={dynamicStyles.section}>
                        <Text style={dynamicStyles.sectionTitle}>Danger Zone</Text>
                        
                        <View style={dynamicStyles.settingItem}>
                            <View style={dynamicStyles.settingInfo}>
                                <Text style={dynamicStyles.settingLabel}>Delete All Data</Text>
                                <Text style={dynamicStyles.settingDescription}>
                                    Permanently delete all habits and progress
                                </Text>
                            </View>
                            <Pressable
                                style={dynamicStyles.dangerButton}
                                onPress={handleDeleteAllData}
                            >
                                <Text style={dynamicStyles.dangerButtonText}>Delete</Text>
                            </Pressable>
                        </View>
                    </View>
                </ScrollView>
            </Animated.View>

            <BottomNavigation activeRoute="settings" />

            <CustomDialog
                visible={dialogVisible}
                title={dialogConfig.title}
                message={dialogConfig.message}
                buttons={dialogConfig.buttons}
                onClose={() => setDialogVisible(false)}
            />
        </View>
    );
}