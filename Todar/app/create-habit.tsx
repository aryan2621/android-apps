import { useRouter } from "expo-router";
import React, { useState } from "react";
import {
    Pressable,
    ScrollView,
    StyleSheet,
    Text,
    TextInput,
    View,
    Animated,
} from "react-native";
import { getHabits, getTodayDate, saveHabits } from "../utils/storage";
import { Habit } from "../types";
import TimePickerModal from "../components/TimePickerModal";
import BottomNavigation from "../components/BottomNavigation";
import { useTheme } from "../contexts/ThemeContext";
import CustomDialog from "../components/CustomDialog";

const HABIT_ICONS = [
    "🏃‍♂️", "🏋️‍♀️", "🧘‍♀️", "📚", "✍️", "💧", "🍎", "😴",
    "🎯", "🎨", "🎵", "🌱", "📱", "🚶‍♀️", "🏊‍♂️", "🚴‍♀️"
];

export default function CreateHabitScreen() {
    const router = useRouter();
    const { theme } = useTheme();
    const [habitName, setHabitName] = useState("");
    const [selectedIcon, setSelectedIcon] = useState("🎯");
    const [notificationTime, setNotificationTime] = useState("9:00 AM");
    const [showTimePicker, setShowTimePicker] = useState(false);
    const [fadeAnim] = useState(new Animated.Value(0));
    const [dialogVisible, setDialogVisible] = useState(false);
    const [dialogConfig, setDialogConfig] = useState({
        title: '',
        message: '',
        buttons: [] as Array<{ text: string; onPress: () => void; style?: 'default' | 'cancel' | 'destructive' }>,
    });

    React.useEffect(() => {
        Animated.timing(fadeAnim, {
            toValue: 1,
            duration: 300,
            useNativeDriver: true,
        }).start();
    }, []);

    const validateHabit = () => {
        const errors: string[] = [];
        
        if (!habitName.trim()) {
            errors.push("• Habit name is required");
        } else if (habitName.trim().length < 2) {
            errors.push("• Habit name must be at least 2 characters");
        } else if (habitName.trim().length > 50) {
            errors.push("• Habit name must be less than 50 characters");
        }
        
        if (!selectedIcon) {
            errors.push("• Please select an icon");
        }
        
        if (!notificationTime || !notificationTime.match(/^\d{1,2}:\d{2}\s?(AM|PM)$/i)) {
            errors.push("• Please set a valid notification time");
        }
        
        return errors;
    };

    const showDialog = (title: string, message: string, buttons: Array<{ text: string; onPress: () => void; style?: 'default' | 'cancel' | 'destructive' }>) => {
        setDialogConfig({ title, message, buttons });
        setDialogVisible(true);
    };

    const handleCreateHabit = async () => {
        const validationErrors = validateHabit();
        
        if (validationErrors.length > 0) {
            showDialog(
                "Please Complete All Fields",
                validationErrors.join("\n"),
                [{ text: "OK", onPress: () => {} }]
            );
            return;
        }

        try {
            const existingHabits = await getHabits();
            
            const duplicateHabit = existingHabits.find(
                habit => habit.name.toLowerCase() === habitName.trim().toLowerCase()
            );
            
            if (duplicateHabit) {
                showDialog(
                    "Duplicate Habit",
                    `A habit named "${habitName.trim()}" already exists. Please choose a different name.`,
                    [{ text: "OK", onPress: () => {} }]
                );
                return;
            }
            
            const newHabit: Habit = {
                id: Date.now().toString() + Math.random(),
                name: habitName.trim(),
                icon: selectedIcon,
                notificationTimes: [notificationTime],
                streak: 0,
                lastCompleted: null,
                createdAt: getTodayDate(),
            };

            const updatedHabits = [...existingHabits, newHabit];
            await saveHabits(updatedHabits);

            showDialog(
                "Success!",
                `"${habitName.trim()}" has been added to your habits`,
                [
                    {
                        text: "Add Another",
                        onPress: () => {
                            setHabitName("");
                            setSelectedIcon("🎯");
                            setNotificationTime("9:00 AM");
                        }
                    },
                    {
                        text: "View Habits",
                        onPress: () => router.push("/home")
                    }
                ]
            );
        } catch (error) {
            showDialog(
                "Error",
                "Failed to create habit. Please try again.",
                [{ text: "OK", onPress: () => {} }]
            );
        }
    };

    const handleTimeChange = (timeString: string) => {
        setNotificationTime(timeString);
        setShowTimePicker(false);
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
        input: {
            backgroundColor: theme.colors.card,
            borderRadius: theme.radius.md,
            padding: theme.spacing.md,
            fontSize: theme.typography.fontSize.base,
            borderWidth: 2,
            borderColor: theme.colors.border,
            color: theme.colors.text,
            ...theme.shadows.sm,
        },
        inputFocused: {
            borderColor: theme.colors.primary,
        },
        iconGrid: {
            flexDirection: "row",
            flexWrap: "wrap",
            gap: theme.spacing.sm,
        },
        iconButton: {
            width: 60,
            height: 60,
            borderRadius: theme.radius.md,
            backgroundColor: theme.colors.card,
            alignItems: "center",
            justifyContent: "center",
            borderWidth: 2,
            borderColor: theme.colors.border,
        },
        iconButtonSelected: {
            backgroundColor: theme.colors.primary,
            borderWidth: 0,
            borderColor: 'transparent',
            transform: [{ scale: 1.1 }],
        },
        iconText: {
            fontSize: 28,
        },
        iconTextSelected: {
            fontSize: 28,
            color: theme.colors.textInverse,
        },
        timeContainer: {
            backgroundColor: theme.colors.card,
            borderRadius: theme.radius.md,
            padding: theme.spacing.lg,
            alignItems: "center",
            borderWidth: 2,
            borderColor: theme.colors.border,
            ...theme.shadows.md,
        },
        timeText: {
            fontSize: theme.typography.fontSize['3xl'],
            fontWeight: "800",
            color: theme.colors.text,
            marginBottom: theme.spacing.xs,
        },
        timeSubtext: {
            fontSize: theme.typography.fontSize.sm,
            color: theme.colors.textSecondary,
            fontWeight: "600",
        },
        footer: {
            padding: theme.spacing.lg,
            backgroundColor: theme.colors.elevated,
            borderTopWidth: 1,
            borderTopColor: theme.colors.border,
        },
        createButton: {
            backgroundColor: theme.colors.primary,
            paddingVertical: theme.spacing.md,
            borderRadius: theme.radius.md,
            alignItems: "center",
            ...theme.shadows.md,
        },
        createButtonDisabled: {
            backgroundColor: theme.colors.disabled,
        },
        createButtonText: {
            color: theme.colors.textInverse,
            fontSize: theme.typography.fontSize.lg,
            fontWeight: "700",
        },
        createButtonTextDisabled: {
            color: theme.colors.disabledText,
        },
        characterCount: {
            fontSize: theme.typography.fontSize.xs,
            color: theme.colors.textTertiary,
            textAlign: "right",
            marginTop: theme.spacing.xs,
        },
    });

    const isFormValid = habitName.trim() && selectedIcon && notificationTime;

    return (
        <View style={dynamicStyles.container}>
            <View style={dynamicStyles.header}>
                <View style={dynamicStyles.headerContent}>
                    <Text style={dynamicStyles.headerTitle}>Create Habit</Text>
                    <Text style={dynamicStyles.headerSubtitle}>Build a new routine</Text>
                </View>
            </View>

            <Animated.View style={{ flex: 1, opacity: fadeAnim }}>
                <ScrollView contentContainerStyle={dynamicStyles.content}>
                    <View style={dynamicStyles.section}>
                        <Text style={dynamicStyles.sectionTitle}>Habit Name</Text>
                        <TextInput
                            style={dynamicStyles.input}
                            placeholder="e.g., Morning Run, Read 20 pages"
                            placeholderTextColor={theme.colors.textTertiary}
                            value={habitName}
                            onChangeText={setHabitName}
                            maxLength={50}
                        />
                        <Text style={dynamicStyles.characterCount}>
                            {habitName.length}/50
                        </Text>
                    </View>

                    <View style={dynamicStyles.section}>
                        <Text style={dynamicStyles.sectionTitle}>Choose Icon</Text>
                        <View style={dynamicStyles.iconGrid}>
                            {HABIT_ICONS.map((icon) => (
                                <Pressable
                                    key={icon}
                                    style={[
                                        dynamicStyles.iconButton,
                                        selectedIcon === icon && dynamicStyles.iconButtonSelected
                                    ]}
                                    onPress={() => setSelectedIcon(icon)}
                                    accessibilityLabel={`Select ${icon} icon`}
                                    accessibilityRole="button"
                                >
                                    <Text style={[
                                        dynamicStyles.iconText,
                                        selectedIcon === icon && dynamicStyles.iconTextSelected
                                    ]}>{icon}</Text>
                                </Pressable>
                            ))}
                        </View>
                    </View>

                    <View style={dynamicStyles.section}>
                        <Text style={dynamicStyles.sectionTitle}>Notification Time</Text>
                        <Pressable 
                            style={dynamicStyles.timeContainer}
                            onPress={() => setShowTimePicker(true)}
                            accessibilityLabel="Set notification time"
                            accessibilityRole="button"
                        >
                            <Text style={dynamicStyles.timeText}>{notificationTime}</Text>
                            <Text style={dynamicStyles.timeSubtext}>Tap to change time</Text>
                        </Pressable>
                    </View>
                </ScrollView>
            </Animated.View>

            <View style={dynamicStyles.footer}>
                <Pressable
                    style={[
                        dynamicStyles.createButton, 
                        !isFormValid && dynamicStyles.createButtonDisabled
                    ]}
                    onPress={handleCreateHabit}
                    disabled={!isFormValid}
                    accessibilityLabel="Create habit"
                    accessibilityRole="button"
                    accessibilityState={{ disabled: !isFormValid }}
                >
                    <Text style={[
                        dynamicStyles.createButtonText,
                        !isFormValid && dynamicStyles.createButtonTextDisabled
                    ]}>
                        Create Habit
                    </Text>
                </Pressable>
            </View>

            <BottomNavigation activeRoute="create" />

            {showTimePicker && (
                <TimePickerModal
                    visible={showTimePicker}
                    initialTime={notificationTime}
                    onConfirm={handleTimeChange}
                    onCancel={() => setShowTimePicker(false)}
                />
            )}

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