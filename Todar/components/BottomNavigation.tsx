import React from 'react';
import { View, Text, StyleSheet, Pressable } from 'react-native';
import { useRouter } from 'expo-router';
import { useTheme } from '../contexts/ThemeContext';

interface BottomNavigationProps {
    activeRoute: 'home' | 'create' | 'settings';
}

export default function BottomNavigation({ activeRoute }: BottomNavigationProps) {
    const router = useRouter();
    const { theme } = useTheme();

    const dynamicStyles = StyleSheet.create({
        bottomNav: {
            flexDirection: "row",
            backgroundColor: theme.colors.elevated,
            paddingVertical: theme.spacing.sm,
            paddingBottom: theme.spacing.md,
            paddingHorizontal: theme.spacing.md,
            borderTopWidth: 1,
            borderTopColor: theme.colors.border,
            justifyContent: "space-around",
            ...theme.shadows.lg,
        },
        navItem: {
            alignItems: "center",
            justifyContent: "center",
            paddingVertical: theme.spacing.sm,
            paddingHorizontal: theme.spacing.lg,
            borderRadius: theme.radius.xl,
            minWidth: 70,
            gap: 4,
        },
        navItemActive: {
            backgroundColor: theme.colors.primary,
            transform: [{ scale: 1.05 }],
        },
        navIcon: {
            fontSize: 24,
        },
        navLabel: {
            fontSize: theme.typography.fontSize.xs,
            fontWeight: '600',
            color: theme.colors.textSecondary,
        },
        navLabelActive: {
            color: theme.colors.textInverse,
            fontWeight: '700',
        },
    });

    const navItems = [
        {
            id: 'home' as const,
            icon: '🏠',
            label: 'Home',
            route: '/home',
        },
        {
            id: 'create' as const,
            icon: '➕',
            label: 'Create',
            route: '/create-habit',
        },
        {
            id: 'settings' as const,
            icon: '⚙️',
            label: 'Settings',
            route: '/settings',
        },
    ];

    const handleNavigation = (route: string) => {
        router.push(route);
    };

    return (
        <View style={dynamicStyles.bottomNav}>
            {navItems.map((item) => {
                const isActive = activeRoute === item.id;
                return (
                    <Pressable
                        key={item.id}
                        style={[
                            dynamicStyles.navItem,
                            isActive && dynamicStyles.navItemActive,
                        ]}
                        onPress={() => handleNavigation(item.route)}
                        accessibilityLabel={item.label}
                        accessibilityRole="button"
                        accessibilityState={{ selected: isActive }}
                    >
                        <Text style={dynamicStyles.navIcon}>{item.icon}</Text>
                        <Text style={[
                            dynamicStyles.navLabel,
                            isActive && dynamicStyles.navLabelActive
                        ]}>
                            {item.label}
                        </Text>
                    </Pressable>
                );
            })}
        </View>
    );
}