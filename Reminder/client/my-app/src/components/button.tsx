import { View, Text, TouchableOpacity, StyleSheet } from 'react-native';
import React from 'react';

const Button = ({ text, bgColor, btnFunction }) => {
    return (
        <View>
            <TouchableOpacity
                style={[
                    styles.button,
                    {
                        backgroundColor: bgColor,
                    },
                ]}
                onPress={btnFunction}
            >
                <Text style={styles.buttonText}>{text}</Text>
            </TouchableOpacity>
        </View>
    );
};

const styles = StyleSheet.create({
    button: {
        padding: 10,
        borderRadius: 8,
        alignItems: 'center',
        marginTop: 10,
    },
    buttonText: {
        color: 'white',
        fontSize: 14,
    },
});

export default Button;
