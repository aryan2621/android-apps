import { useRouter } from "expo-router";
import React, { useEffect, useState } from "react";
import { Pressable, StyleSheet, Text, View, Animated } from "react-native";
import { getHabits } from "../utils/storage";
import { useTheme } from "../contexts/ThemeContext";

export default function WelcomeScreen() {
    const router = useRouter();
    const { theme } = useTheme();
    const [scaleAnim] = useState(new Animated.Value(0.8));
    const [fadeAnim] = useState(new Animated.Value(0));
    const [buttonScale] = useState(new Animated.Value(1));

    useEffect(() => {
        checkFirstTime();
        
        Animated.parallel([
            Animated.spring(scaleAnim, {
                toValue: 1,
                tension: 50,
                friction: 7,
                useNativeDriver: true,
            }),
            Animated.timing(fadeAnim, {
                toValue: 1,
                duration: 500,
                useNativeDriver: true,
            }),
        ]).start();
    }, []);

    const checkFirstTime = async () => {
        const habits = await getHabits();
        if (habits.length > 0) {
            router.replace("/home");
        }
    };

    const handleGetStarted = () => {
        Animated.sequence([
            Animated.timing(buttonScale, {
                toValue: 0.95,
                duration: 100,
                useNativeDriver: true,
            }),
            Animated.timing(buttonScale, {
                toValue: 1,
                duration: 100,
                useNativeDriver: true,
            }),
        ]).start(() => {
            router.push("/select-habits");
        });
    };

    const dynamicStyles = StyleSheet.create({
        container: {
            flex: 1,
            backgroundColor: theme.colors.background,
            alignItems: "center",
            justifyContent: "center",
            padding: theme.spacing.lg,
            paddingTop: theme.spacing['2xl'],
            paddingBottom: theme.spacing['2xl'],
        },
        mascotContainer: {
            marginBottom: theme.spacing['2xl'],
        },
        mascot: {
            width: 180,
            height: 180,
            borderRadius: theme.radius.full,
            backgroundColor: theme.colors.primary,
            alignItems: "center",
            justifyContent: "center",
            ...theme.shadows.lg,
        },
        mascotText: {
            fontSize: 90,
        },
        title: {
            fontSize: theme.typography.fontSize['4xl'],
            fontWeight: "800",
            color: theme.colors.text,
            marginBottom: theme.spacing.lg,
            textAlign: "center",
        },
        subtitle: {
            fontSize: theme.typography.fontSize.base,
            color: theme.colors.textSecondary,
            textAlign: "center",
            lineHeight: 24,
            marginBottom: theme.spacing['3xl'],
            paddingHorizontal: theme.spacing.lg,
            fontWeight: "500",
        },
        button: {
            backgroundColor: theme.colors.primary,
            paddingVertical: theme.spacing.lg,
            paddingHorizontal: theme.spacing['3xl'],
            borderRadius: theme.radius.xl,
            width: "85%",
            alignItems: "center",
            ...theme.shadows.md,
        },
        buttonText: {
            color: theme.colors.textInverse,
            fontSize: theme.typography.fontSize.lg,
            fontWeight: "700",
        },
        features: {
            marginTop: theme.spacing['2xl'],
            gap: theme.spacing.md,
        },
        featureItem: {
            flexDirection: "row",
            alignItems: "center",
            gap: theme.spacing.sm,
        },
        featureIcon: {
            fontSize: 20,
        },
        featureText: {
            fontSize: theme.typography.fontSize.sm,
            color: theme.colors.textSecondary,
            fontWeight: "500",
        },
    });

    return (
        <Animated.View 
            style={[
                dynamicStyles.container,
                { opacity: fadeAnim }
            ]}
        >
            <Animated.View 
                style={[
                    dynamicStyles.mascotContainer,
                    { transform: [{ scale: scaleAnim }] }
                ]}
            >
                <View style={dynamicStyles.mascot}>
                    <Text style={dynamicStyles.mascotText}>🦉</Text>
                </View>
            </Animated.View>

            <Text style={dynamicStyles.title}>Welcome to Todar</Text>
            <Text style={dynamicStyles.subtitle}>
                Build lasting habits with daily streaks. Stay motivated and track your progress!
            </Text>

            <Animated.View style={{ transform: [{ scale: buttonScale }], alignItems: 'center', width: '100%' }}>
                <Pressable 
                    style={dynamicStyles.button} 
                    onPress={handleGetStarted}
                    accessibilityLabel="Get started"
                    accessibilityRole="button"
                >
                    <Text style={dynamicStyles.buttonText}>Get Started</Text>
                </Pressable>
            </Animated.View>

            <View style={dynamicStyles.features}>
                <View style={dynamicStyles.featureItem}>
                    <Text style={dynamicStyles.featureIcon}>🔥</Text>
                    <Text style={dynamicStyles.featureText}>Track daily streaks</Text>
                </View>
                <View style={dynamicStyles.featureItem}>
                    <Text style={dynamicStyles.featureIcon}>🔔</Text>
                    <Text style={dynamicStyles.featureText}>Get daily reminders</Text>
                </View>
                <View style={dynamicStyles.featureItem}>
                    <Text style={dynamicStyles.featureIcon}>📈</Text>
                    <Text style={dynamicStyles.featureText}>Build consistent habits</Text>
                </View>
            </View>
        </Animated.View>
    );
}