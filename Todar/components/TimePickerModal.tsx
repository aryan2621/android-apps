import React, { useState, useRef } from "react";
import {
    Modal,
    View,
    Text,
    StyleSheet,
    Pressable,
    Animated,
    Dimensions,
    ScrollView,
} from "react-native";
import { useTheme } from "../contexts/ThemeContext";

interface CustomTimePickerProps {
    visible: boolean;
    initialTime: string; // "HH:MM" format
    onConfirm: (time: string) => void;
    onCancel: () => void;
}

export default function CustomTimePicker({
    visible,
    initialTime,
    onConfirm,
    onCancel,
}: CustomTimePickerProps) {
    const { theme } = useTheme();
    // Parse initial time - handle both 24-hour (HH:MM) and 12-hour (H:MM AM/PM) formats
    const parseInitialTime = (time: string) => {
        console.log('Parsing time:', time);
        if (time.includes('AM') || time.includes('PM')) {
            // 12-hour format
            const parts = time.split(' ');
            const timePart = parts[0];
            const period = parts[1];
            const [hours, minutes] = timePart.split(':').map(Number);
            console.log('12-hour parsed:', { hours, minutes, period });
            return { hours, minutes, period };
        } else {
            // 24-hour format
            const [hours, minutes] = time.split(':').map(Number);
            const period = hours >= 12 ? 'PM' : 'AM';
            const hour12 = hours > 12 ? hours - 12 : (hours === 0 ? 12 : hours);
            console.log('24-hour parsed:', { hours: hour12, minutes, period });
            return { hours: hour12, minutes, period };
        }
    };

    const { hours, minutes, period } = parseInitialTime(initialTime);
    const [selectedHour, setSelectedHour] = useState(isNaN(hours) ? 9 : hours);
    const [selectedMinute, setSelectedMinute] = useState(isNaN(minutes) ? 0 : minutes);
    const [selectedPeriod, setSelectedPeriod] = useState(period || 'AM');
    const [slideAnim] = useState(new Animated.Value(Dimensions.get("window").height));
    const [fadeAnim] = useState(new Animated.Value(0));

    const hourScrollRef = useRef<ScrollView>(null);
    const minuteScrollRef = useRef<ScrollView>(null);
    const periodScrollRef = useRef<ScrollView>(null);

    React.useEffect(() => {
        if (visible) {
            Animated.parallel([
                Animated.spring(slideAnim, {
                    toValue: 0,
                    tension: 65,
                    friction: 9,
                    useNativeDriver: true,
                }),
                Animated.timing(fadeAnim, {
                    toValue: 1,
                    duration: 250,
                    useNativeDriver: true,
                }),
            ]).start();
            
            // Scroll to initial positions after animation
            setTimeout(() => {
                hourScrollRef.current?.scrollTo({
                    y: (selectedHour - 1) * ITEM_HEIGHT,
                    animated: false,
                });
                minuteScrollRef.current?.scrollTo({
                    y: selectedMinute * ITEM_HEIGHT,
                    animated: false,
                });
                periodScrollRef.current?.scrollTo({
                    y: (selectedPeriod === 'AM' ? 0 : 1) * ITEM_HEIGHT,
                    animated: false,
                });
            }, 100);
        } else {
            Animated.parallel([
                Animated.timing(slideAnim, {
                    toValue: Dimensions.get("window").height,
                    duration: 200,
                    useNativeDriver: true,
                }),
                Animated.timing(fadeAnim, {
                    toValue: 0,
                    duration: 200,
                    useNativeDriver: true,
                }),
            ]).start();
        }
    }, [visible]);

    const handleConfirm = () => {
        const timeString = `${selectedHour}:${selectedMinute.toString().padStart(2, "0")} ${selectedPeriod}`;
        onConfirm(timeString);
    };

    const hours12 = Array.from({ length: 12 }, (_, i) => i + 1);
    const minutes60 = Array.from({ length: 60 }, (_, i) => i);
    const periods = ['AM', 'PM'];

    const ITEM_HEIGHT = 56;

    const dynamicStyles = StyleSheet.create({
        overlay: {
            flex: 1,
            backgroundColor: "rgba(0, 0, 0, 0.5)",
            justifyContent: "center",
            alignItems: "center",
        },
        backdrop: {
            flex: 1,
            width: "100%",
        },
        modalContent: {
            backgroundColor: theme.colors.card,
            borderRadius: theme.radius.xl,
            marginHorizontal: theme.spacing.lg,
            maxWidth: 400,
            width: "100%",
            ...theme.shadows.xl,
        },
        header: {
            paddingVertical: theme.spacing.xl,
            paddingHorizontal: theme.spacing.xl,
            alignItems: "center",
            borderBottomWidth: 1,
            borderBottomColor: theme.colors.border,
        },
        title: {
            fontSize: theme.typography.fontSize["2xl"],
            fontWeight: "800",
            color: theme.colors.text,
            marginBottom: theme.spacing.xs,
        },
        subtitle: {
            fontSize: theme.typography.fontSize.base,
            color: theme.colors.textSecondary,
            fontWeight: "500",
        },
        pickerContainer: {
            flexDirection: "row",
            alignItems: "center",
            justifyContent: "center",
            paddingVertical: theme.spacing.lg,
            paddingHorizontal: theme.spacing.md,
            position: "relative",
        },
        scrollContainer: {
            height: ITEM_HEIGHT * 3,
            width: 70,
            borderRadius: theme.radius.lg,
            backgroundColor: theme.colors.surface,
            borderWidth: 2,
            borderColor: theme.colors.border,
        },
        pickerColumn: {
            width: 70,
            alignItems: "center",
        },
        separator: {
            fontSize: 32,
            fontWeight: "800",
            color: theme.colors.primary,
            marginHorizontal: theme.spacing.sm,
        },
        timeItem: {
            height: ITEM_HEIGHT,
            justifyContent: "center",
            alignItems: "center",
            width: "100%",
        },
        timeText: {
            fontSize: 24,
            fontWeight: "600",
            color: theme.colors.textTertiary,
        },
        selectedTimeText: {
            fontSize: 28,
            fontWeight: "800",
            color: theme.colors.primary,
        },
        selectionHighlight: {
            position: "absolute",
            top: ITEM_HEIGHT + 28,
            left: theme.spacing.xl,
            right: theme.spacing.xl,
            height: ITEM_HEIGHT,
            backgroundColor: theme.colors.primary + "15",
            borderRadius: theme.radius.md,
            borderWidth: 2,
            borderColor: theme.colors.primary + "40",
        },
        buttons: {
            flexDirection: "row",
            paddingHorizontal: theme.spacing.xl,
            paddingBottom: theme.spacing.xl,
            gap: theme.spacing.md,
        },
        cancelButton: {
            flex: 1,
            paddingVertical: theme.spacing.lg,
            borderRadius: theme.radius.lg,
            backgroundColor: theme.colors.surface,
            alignItems: "center",
            borderWidth: 2,
            borderColor: theme.colors.border,
        },
        cancelText: {
            fontSize: theme.typography.fontSize.lg,
            fontWeight: "700",
            color: theme.colors.textSecondary,
        },
        confirmButton: {
            flex: 1,
            paddingVertical: theme.spacing.lg,
            borderRadius: theme.radius.lg,
            backgroundColor: theme.colors.primary,
            alignItems: "center",
            ...theme.shadows.md,
        },
        confirmText: {
            fontSize: theme.typography.fontSize.lg,
            fontWeight: "700",
            color: theme.colors.textInverse,
        },
    });

    const timeFormatted = {
        hour12: selectedHour,
        minute: selectedMinute,
        period: selectedPeriod,
        display: `${selectedHour.toString().padStart(2, "0")}:${selectedMinute.toString().padStart(2, "0")}`,
        display12Hour: `${selectedHour}:${selectedMinute.toString().padStart(2, "0")} ${selectedPeriod}`,
    };

    const renderTimeItem = (value: number, isSelected: boolean, type: "hour" | "minute") => {
        return (
            <Pressable
                key={value}
                style={dynamicStyles.timeItem}
                onPress={() => {
                    if (type === "hour") {
                        setSelectedHour(value);
                    } else {
                        setSelectedMinute(value);
                    }
                }}
            >
                <Text
                    style={[
                        dynamicStyles.timeText,
                        isSelected && dynamicStyles.selectedTimeText,
                    ]}
                >
                    {value.toString().padStart(2, "0")}
                </Text>
            </Pressable>
        );
    };

    const renderPeriodItem = (period: string, isSelected: boolean) => {
        return (
            <Pressable
                key={period}
                style={dynamicStyles.timeItem}
                onPress={() => setSelectedPeriod(period)}
            >
                <Text
                    style={[
                        dynamicStyles.timeText,
                        isSelected && dynamicStyles.selectedTimeText,
                    ]}
                >
                    {period}
                </Text>
            </Pressable>
        );
    };

    return (
        <Modal visible={visible} transparent animationType="none" onRequestClose={onCancel}>
            <Animated.View style={[dynamicStyles.overlay, { opacity: fadeAnim }]}>
                <Pressable style={dynamicStyles.backdrop} onPress={onCancel} />
                <Animated.View
                    style={[
                        dynamicStyles.modalContent,
                        { 
                            transform: [
                                { scale: fadeAnim },
                                { translateY: slideAnim }
                            ] 
                        },
                    ]}
                >
                    <View style={dynamicStyles.header}>
                        <Text style={dynamicStyles.title}>⏰ Set Time</Text>
                        <Text style={dynamicStyles.subtitle}>Choose your reminder time</Text>
                    </View>


                    <View style={dynamicStyles.pickerContainer}>
                        <View style={dynamicStyles.selectionHighlight} />
                        
                        {/* Hours */}
                        <View style={dynamicStyles.scrollContainer}>
                            <ScrollView
                                ref={hourScrollRef}
                                showsVerticalScrollIndicator={false}
                                snapToInterval={ITEM_HEIGHT}
                                decelerationRate="fast"
                                onMomentumScrollEnd={(event) => {
                                    const offset = event.nativeEvent.contentOffset.y;
                                    const index = Math.round(offset / ITEM_HEIGHT);
                                    const clampedIndex = Math.max(1, Math.min(12, index + 1));
                                    setSelectedHour(clampedIndex);
                                    
                                    // Ensure proper alignment
                                    hourScrollRef.current?.scrollTo({
                                        y: (clampedIndex - 1) * ITEM_HEIGHT,
                                        animated: true,
                                    });
                                }}
                                contentContainerStyle={{
                                    paddingVertical: ITEM_HEIGHT,
                                }}
                            >
                                {hours12.map((hour) =>
                                    renderTimeItem(hour, hour === selectedHour, "hour")
                                )}
                            </ScrollView>
                        </View>

                        <Text style={[dynamicStyles.separator, { marginHorizontal: theme.spacing.lg }]}>:</Text>

                        {/* Minutes */}
                        <View style={dynamicStyles.scrollContainer}>
                            <ScrollView
                                ref={minuteScrollRef}
                                showsVerticalScrollIndicator={false}
                                snapToInterval={ITEM_HEIGHT}
                                decelerationRate="fast"
                                onMomentumScrollEnd={(event) => {
                                    const offset = event.nativeEvent.contentOffset.y;
                                    const index = Math.round(offset / ITEM_HEIGHT);
                                    const clampedIndex = Math.max(0, Math.min(59, index));
                                    setSelectedMinute(clampedIndex);
                                    
                                    // Ensure proper alignment
                                    minuteScrollRef.current?.scrollTo({
                                        y: clampedIndex * ITEM_HEIGHT,
                                        animated: true,
                                    });
                                }}
                                contentContainerStyle={{
                                    paddingVertical: ITEM_HEIGHT,
                                }}
                            >
                                {minutes60.map((minute) =>
                                    renderTimeItem(minute, minute === selectedMinute, "minute")
                                )}
                            </ScrollView>
                        </View>

                        <Text style={[dynamicStyles.separator, { marginHorizontal: theme.spacing.xs }]}> </Text>

                        {/* AM/PM */}
                        <View style={dynamicStyles.scrollContainer}>
                            <ScrollView
                                ref={periodScrollRef}
                                showsVerticalScrollIndicator={false}
                                snapToInterval={ITEM_HEIGHT}
                                decelerationRate="fast"
                                onMomentumScrollEnd={(event) => {
                                    const offset = event.nativeEvent.contentOffset.y;
                                    const index = Math.round(offset / ITEM_HEIGHT);
                                    const clampedIndex = Math.max(0, Math.min(1, index));
                                    setSelectedPeriod(periods[clampedIndex]);
                                    
                                    // Ensure proper alignment
                                    periodScrollRef.current?.scrollTo({
                                        y: clampedIndex * ITEM_HEIGHT,
                                        animated: true,
                                    });
                                }}
                                contentContainerStyle={{
                                    paddingVertical: ITEM_HEIGHT,
                                }}
                            >
                                {periods.map((period) =>
                                    renderPeriodItem(period, period === selectedPeriod)
                                )}
                            </ScrollView>
                        </View>
                    </View>

                    <View style={dynamicStyles.buttons}>
                        <Pressable
                            style={dynamicStyles.cancelButton}
                            onPress={onCancel}
                            accessibilityLabel="Cancel"
                            accessibilityRole="button"
                        >
                            <Text style={dynamicStyles.cancelText}>Cancel</Text>
                        </Pressable>
                        <Pressable
                            style={dynamicStyles.confirmButton}
                            onPress={handleConfirm}
                            accessibilityLabel="Confirm time"
                            accessibilityRole="button"
                        >
                            <Text style={dynamicStyles.confirmText}>✓ Set {timeFormatted.display12Hour}</Text>
                        </Pressable>
                    </View>
                </Animated.View>
            </Animated.View>
        </Modal>
    );
}