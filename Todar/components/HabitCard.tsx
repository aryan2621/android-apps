import React from "react";
import { View, Text, StyleSheet, Pressable } from "react-native";
import { Habit } from "../types";
import { useTheme } from "../contexts/ThemeContext";

interface HabitCardProps {
    habit: Habit;
    onPress: () => void;
    onDelete?: (habitId: string) => void;
}

export default function HabitCard({ habit, onPress, onDelete }: HabitCardProps) {
    const { theme } = useTheme();
    const [isPressed, setIsPressed] = React.useState(false);

    const handleLongPress = () => {
        if (onDelete) {
            onDelete(habit.id);
        }
    };

    const dynamicStyles = StyleSheet.create({
        card: {
            backgroundColor: theme.colors.card,
            borderRadius: theme.radius.lg,
            padding: theme.spacing.lg,
            flexDirection: "row",
            alignItems: "center",
            justifyContent: "space-between",
            marginBottom: theme.spacing.md,
            borderWidth: 2,
            borderColor: isPressed ? theme.colors.primary : theme.colors.border,
            ...theme.shadows.md,
            transform: [{ scale: isPressed ? 0.98 : 1 }],
        },
        left: {
            flexDirection: "row",
            alignItems: "center",
            gap: theme.spacing.md,
            flex: 1,
        },
        iconContainer: {
            width: 56,
            height: 56,
            borderRadius: theme.radius.md,
            backgroundColor: theme.colors.primaryLight + '20',
            alignItems: "center",
            justifyContent: "center",
        },
        icon: {
            fontSize: 32,
            textAlign: "center",
            lineHeight: 32,
        },
        nameContainer: {
            flex: 1,
        },
        name: {
            fontSize: theme.typography.fontSize.lg,
            fontWeight: "700",
            color: theme.colors.text,
            marginBottom: 4,
        },
        hintText: {
            fontSize: theme.typography.fontSize.xs,
            color: theme.colors.textTertiary,
            fontStyle: "italic",
        },
        streakBadge: {
            flexDirection: "row",
            alignItems: "center",
            justifyContent: "center",
            backgroundColor: theme.colors.warningLight + '30',
            paddingHorizontal: theme.spacing.md,
            paddingVertical: theme.spacing.sm,
            borderRadius: theme.radius.full,
            gap: 6,
            minWidth: 60,
        },
        streakIcon: {
            fontSize: 20,
            textAlign: "center",
            lineHeight: 20,
        },
        streakNumber: {
            fontSize: theme.typography.fontSize.xl,
            fontWeight: "800",
            color: theme.colors.streak,
        },
    });

    return (
        <Pressable 
            style={dynamicStyles.card} 
            onPress={onPress}
            onLongPress={handleLongPress}
            onPressIn={() => setIsPressed(true)}
            onPressOut={() => setIsPressed(false)}
            accessibilityLabel={`${habit.name}, ${habit.streak} day streak`}
            accessibilityHint="Tap to mark as complete, long press to delete"
            accessibilityRole="button"
        >
            <View style={dynamicStyles.left}>
                <View style={dynamicStyles.iconContainer}>
                    <Text style={dynamicStyles.icon}>{habit.icon}</Text>
                </View>
                <View style={dynamicStyles.nameContainer}>
                    <Text style={dynamicStyles.name} numberOfLines={1}>
                        {habit.name}
                    </Text>
                    {onDelete && (
                        <Text style={dynamicStyles.hintText}>Long press to delete</Text>
                    )}
                </View>
            </View>
            <View style={dynamicStyles.streakBadge}>
                <Text style={dynamicStyles.streakIcon}>🔥</Text>
                <Text style={dynamicStyles.streakNumber}>{habit.streak}</Text>
            </View>
        </Pressable>
    );
}