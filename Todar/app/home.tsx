import React, { useState, useEffect } from "react";
import {
    View,
    Text,
    StyleSheet,
    Pressable,
    ScrollView,
    RefreshControl,
    ActivityIndicator,
    Animated,
} from "react-native";
import { useRouter } from "expo-router";
import { getHabits, saveHabits } from "../utils/storage";
import { Habit } from "../types";
import HabitCard from "../components/HabitCard";
import { useTheme } from "../contexts/ThemeContext";
import BottomNavigation from "../components/BottomNavigation";
import CustomDialog from "../components/CustomDialog";

export default function HomeScreen() {
    const router = useRouter();
    const { theme } = useTheme();
    const [habits, setHabits] = useState<Habit[]>([]);
    const [refreshing, setRefreshing] = useState(false);
    const [loading, setLoading] = useState(true);
    const [fadeAnim] = useState(new Animated.Value(0));
    const [deleteDialogVisible, setDeleteDialogVisible] = useState(false);
    const [habitToDelete, setHabitToDelete] = useState<{ id: string; name: string } | null>(null);

    useEffect(() => {
        loadData();
    }, []);

    const loadData = async () => {
        setLoading(true);
        try {
            const loadedHabits = await getHabits();
            setHabits(loadedHabits);
            
            Animated.timing(fadeAnim, {
                toValue: 1,
                duration: 300,
                useNativeDriver: true,
            }).start();
        } catch (error) {
            console.error('Failed to load habits:', error);
        } finally {
            setLoading(false);
        }
    };

    const onRefresh = async () => {
        setRefreshing(true);
        await loadData();
        setRefreshing(false);
    };

    const handleHabitPress = (habitId: string) => {
        router.push(`/task-completion?habitId=${habitId}`);
    };

    const handleDeleteHabit = (habitId: string) => {
        const habit = habits.find(h => h.id === habitId);
        if (habit) {
            setHabitToDelete({ id: habit.id, name: habit.name });
            setDeleteDialogVisible(true);
        }
    };

    const confirmDelete = async () => {
        if (!habitToDelete) return;
        
        try {
            const updatedHabits = habits.filter(habit => habit.id !== habitToDelete.id);
            await saveHabits(updatedHabits);
            setHabits(updatedHabits);
            setHabitToDelete(null);
        } catch (error) {
            console.error("Failed to delete habit:", error);
        }
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
            paddingBottom: theme.spacing['2xl'],
        },
        statsCard: {
            backgroundColor: theme.colors.card,
            borderRadius: theme.radius.lg,
            padding: theme.spacing.lg,
            marginBottom: theme.spacing.lg,
            flexDirection: "row",
            justifyContent: "space-around",
            ...theme.shadows.md,
            borderWidth: 1,
            borderColor: theme.colors.border,
        },
        statItem: {
            alignItems: "center",
        },
        statNumber: {
            fontSize: theme.typography.fontSize['2xl'],
            fontWeight: "800",
            color: theme.colors.primary,
            marginBottom: 4,
        },
        statLabel: {
            fontSize: theme.typography.fontSize.sm,
            color: theme.colors.textSecondary,
            fontWeight: "600",
        },
        sectionHeader: {
            fontSize: theme.typography.fontSize.lg,
            fontWeight: "700",
            color: theme.colors.text,
            marginBottom: theme.spacing.md,
            marginTop: theme.spacing.sm,
        },
        emptyState: {
            alignItems: "center",
            justifyContent: "center",
            paddingVertical: theme.spacing['3xl'],
        },
        emptyIcon: {
            fontSize: 80,
            marginBottom: theme.spacing.lg,
        },
        emptyTitle: {
            fontSize: theme.typography.fontSize.xl,
            fontWeight: "700",
            color: theme.colors.text,
            marginBottom: theme.spacing.sm,
            textAlign: "center",
        },
        emptyText: {
            fontSize: theme.typography.fontSize.base,
            color: theme.colors.textSecondary,
            marginBottom: theme.spacing.xl,
            textAlign: "center",
            paddingHorizontal: theme.spacing.lg,
        },
        addButton: {
            backgroundColor: theme.colors.primary,
            paddingVertical: theme.spacing.md,
            paddingHorizontal: theme.spacing.xl,
            borderRadius: theme.radius.xl,
            ...theme.shadows.md,
        },
        addButtonText: {
            color: theme.colors.textInverse,
            fontSize: theme.typography.fontSize.base,
            fontWeight: "700",
        },
        loadingContainer: {
            flex: 1,
            justifyContent: "center",
            alignItems: "center",
            backgroundColor: theme.colors.background,
        },
        loadingContent: {
            alignItems: "center",
            justifyContent: "center",
        },
        loadingIcon: {
            width: 80,
            height: 80,
            borderRadius: theme.radius.full,
            backgroundColor: theme.colors.successLight,
            alignItems: "center",
            justifyContent: "center",
            marginBottom: theme.spacing.xl,
        },
        loadingEmoji: {
            fontSize: 40,
        },
        loadingText: {
            fontSize: theme.typography.fontSize.lg,
            fontWeight: "700",
            color: theme.colors.text,
            marginTop: theme.spacing.md,
            marginBottom: theme.spacing.sm,
        },
        loadingSubtext: {
            fontSize: theme.typography.fontSize.base,
            color: theme.colors.textSecondary,
            textAlign: "center",
        },
    });

    if (loading) {
        return (
            <View style={dynamicStyles.container}>
                <View style={dynamicStyles.header}>
                    <View style={dynamicStyles.headerContent}>
                        <Text style={dynamicStyles.headerTitle}>Your Daily Streaks</Text>
                        <Text style={dynamicStyles.headerSubtitle}>Track your progress</Text>
                    </View>
                </View>
            
                <View style={dynamicStyles.loadingContainer}>
                    <View style={dynamicStyles.loadingContent}>
                        <View style={dynamicStyles.loadingIcon}>
                            <Text style={dynamicStyles.loadingEmoji}>⚡</Text>
                        </View>
                        <ActivityIndicator 
                            size="large" 
                            color={theme.colors.primary}
                        />
                        <Text style={dynamicStyles.loadingText}>Loading your habits...</Text>
                        <Text style={dynamicStyles.loadingSubtext}>Building your streak momentum</Text>
                    </View>
                </View>
            </View>
        );
    }

    const totalHabits = habits.length;
    const totalStreaks = habits.reduce((sum, habit) => sum + habit.streak, 0);
    const longestStreak = Math.max(...habits.map(h => h.streak), 0);

    return (
        <View style={dynamicStyles.container}>
            <View style={dynamicStyles.header}>
                <View style={dynamicStyles.headerContent}>
                    <Text style={dynamicStyles.headerTitle}>Your Daily Streaks</Text>
                    <Text style={dynamicStyles.headerSubtitle}>Keep the momentum going!</Text>
                </View>
            </View>

            <Animated.View style={{ flex: 1, opacity: fadeAnim }}>
                <ScrollView
                    contentContainerStyle={dynamicStyles.content}
                    refreshControl={
                        <RefreshControl
                            refreshing={refreshing}
                            onRefresh={onRefresh}
                            tintColor={theme.colors.primary}
                        />
                    }
                >
                    {habits.length > 0 && (
                        <View style={dynamicStyles.statsCard}>
                            <View style={dynamicStyles.statItem}>
                                <Text style={dynamicStyles.statNumber}>{totalHabits}</Text>
                                <Text style={dynamicStyles.statLabel}>Active</Text>
                            </View>
                            <View style={dynamicStyles.statItem}>
                                <Text style={dynamicStyles.statNumber}>{totalStreaks}</Text>
                                <Text style={dynamicStyles.statLabel}>Total Days</Text>
                            </View>
                            <View style={dynamicStyles.statItem}>
                                <Text style={dynamicStyles.statNumber}>{longestStreak}</Text>
                                <Text style={dynamicStyles.statLabel}>Best Streak</Text>
                            </View>
                        </View>
                    )}

                    {habits.length === 0 ? (
                        <View style={dynamicStyles.emptyState}>
                            <Text style={dynamicStyles.emptyIcon}>🎯</Text>
                            <Text style={dynamicStyles.emptyTitle}>No habits yet!</Text>
                            <Text style={dynamicStyles.emptyText}>
                                Start building positive habits today and watch your streaks grow!
                            </Text>
                            <Pressable
                                style={dynamicStyles.addButton}
                                onPress={() => router.push("/select-habits")}
                            >
                                <Text style={dynamicStyles.addButtonText}>
                                    Add Your First Habit
                                </Text>
                            </Pressable>
                        </View>
                    ) : (
                        <>
                            <Text style={dynamicStyles.sectionHeader}>Your Habits</Text>
                            {habits.map((habit) => (
                                <HabitCard
                                    key={habit.id}
                                    habit={habit}
                                    onPress={() => handleHabitPress(habit.id)}
                                    onDelete={handleDeleteHabit}
                                />
                            ))}
                        </>
                    )}
                </ScrollView>
            </Animated.View>

            <BottomNavigation activeRoute="home" />

            <CustomDialog
                visible={deleteDialogVisible}
                title="Delete Habit?"
                message={`Are you sure you want to delete "${habitToDelete?.name}"? This will remove all progress and cannot be undone.`}
                buttons={[
                    {
                        text: "Cancel",
                        style: "cancel",
                        onPress: () => {
                            setDeleteDialogVisible(false);
                            setHabitToDelete(null);
                        },
                    },
                    {
                        text: "Delete",
                        style: "destructive",
                        onPress: confirmDelete,
                    },
                ]}
                onClose={() => {
                    setDeleteDialogVisible(false);
                    setHabitToDelete(null);
                }}
            />
        </View>
    );
}