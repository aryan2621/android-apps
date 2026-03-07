import React from 'react';
import {
    View,
    Text,
    StyleSheet,
    Pressable,
    Modal,
    Animated,
    Dimensions,
} from 'react-native';
import { useTheme } from '../contexts/ThemeContext';

interface DialogButton {
    text: string;
    onPress: () => void;
    style?: 'default' | 'cancel' | 'destructive';
}

interface CustomDialogProps {
    visible: boolean;
    title: string;
    message: string;
    buttons: DialogButton[];
    onClose: () => void;
}

export default function CustomDialog({
    visible,
    title,
    message,
    buttons,
    onClose,
}: CustomDialogProps) {
    const { theme } = useTheme();
    const [fadeAnim] = React.useState(new Animated.Value(0));
    const [scaleAnim] = React.useState(new Animated.Value(0.8));

    React.useEffect(() => {
        if (visible) {
            Animated.parallel([
                Animated.timing(fadeAnim, {
                    toValue: 1,
                    duration: 200,
                    useNativeDriver: true,
                }),
                Animated.spring(scaleAnim, {
                    toValue: 1,
                    tension: 100,
                    friction: 8,
                    useNativeDriver: true,
                }),
            ]).start();
        } else {
            Animated.parallel([
                Animated.timing(fadeAnim, {
                    toValue: 0,
                    duration: 150,
                    useNativeDriver: true,
                }),
                Animated.timing(scaleAnim, {
                    toValue: 0.8,
                    duration: 150,
                    useNativeDriver: true,
                }),
            ]).start();
        }
    }, [visible]);

    const getButtonStyle = (style: string) => {
        switch (style) {
            case 'destructive':
                return {
                    backgroundColor: theme.colors.danger,
                    color: '#fff',
                };
            case 'cancel':
                return {
                    backgroundColor: theme.colors.border,
                    color: theme.colors.text,
                };
            default:
                return {
                    backgroundColor: theme.colors.primary,
                    color: '#fff',
                };
        }
    };

    const dynamicStyles = StyleSheet.create({
        overlay: {
            flex: 1,
            backgroundColor: 'rgba(0, 0, 0, 0.5)',
            justifyContent: 'center',
            alignItems: 'center',
            padding: 20,
        },
        dialog: {
            backgroundColor: theme.colors.card,
            borderRadius: 20,
            padding: 24,
            width: '100%',
            maxWidth: 400,
            shadowColor: theme.colors.shadow,
            shadowOffset: {
                width: 0,
                height: 10,
            },
            shadowOpacity: 0.25,
            shadowRadius: 20,
            elevation: 20,
        },
        title: {
            fontSize: 20,
            fontWeight: 'bold',
            color: theme.colors.text,
            marginBottom: 12,
            textAlign: 'center',
        },
        message: {
            fontSize: 16,
            color: theme.colors.textSecondary,
            marginBottom: 24,
            textAlign: 'center',
            lineHeight: 22,
        },
        buttonContainer: {
            flexDirection: 'row',
            justifyContent: 'space-between',
            gap: 12,
        },
        button: {
            flex: 1,
            paddingVertical: 12,
            paddingHorizontal: 20,
            borderRadius: 12,
            alignItems: 'center',
            justifyContent: 'center',
        },
        buttonText: {
            fontSize: 16,
            fontWeight: '600',
        },
    });

    if (!visible) return null;

    return (
        <Modal
            transparent
            visible={visible}
            animationType="none"
            onRequestClose={onClose}
        >
            <Animated.View
                style={[
                    dynamicStyles.overlay,
                    { opacity: fadeAnim },
                ]}
            >
                <Pressable
                    style={StyleSheet.absoluteFill}
                    onPress={onClose}
                />
                <Animated.View
                    style={[
                        dynamicStyles.dialog,
                        {
                            transform: [{ scale: scaleAnim }],
                        },
                    ]}
                >
                    <Text style={dynamicStyles.title}>{title}</Text>
                    <Text style={dynamicStyles.message}>{message}</Text>
                    
                    <View style={dynamicStyles.buttonContainer}>
                        {buttons.map((button, index) => {
                            const buttonStyle = getButtonStyle(button.style || 'default');
                            return (
                                <Pressable
                                    key={index}
                                    style={[
                                        dynamicStyles.button,
                                        { backgroundColor: buttonStyle.backgroundColor },
                                    ]}
                                    onPress={() => {
                                        button.onPress();
                                        onClose();
                                    }}
                                >
                                    <Text
                                        style={[
                                            dynamicStyles.buttonText,
                                            { color: buttonStyle.color },
                                        ]}
                                    >
                                        {button.text}
                                    </Text>
                                </Pressable>
                            );
                        })}
                    </View>
                </Animated.View>
            </Animated.View>
        </Modal>
    );
}
