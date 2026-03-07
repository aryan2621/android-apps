import { View, Text, TextInput, useColorScheme, StyleSheet, Alert } from 'react-native';
import React, { useState } from 'react';
import { SignUpUser } from '../model';
import { signUp } from '../service/user';
import { ImageBackground } from 'react-native';
import Button from '../components/button';

const emailRegex =
    /^(([^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|.(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;

const SignUp = ({ navigation }: any) => {
    const darkMode = useColorScheme() === 'dark';
    const [user, setUser] = useState(new SignUpUser({}));
    const [focusedInput, setFocusedInput] = useState<string | null>(null);

    const handleUserChange = (value: any, name: any) => {
        setUser({
            ...user,
            [name]: value,
        });
    };

    const handleSignUp = async () => {
        user.name = user?.name?.trim() ?? '';
        user.email = user?.email?.trim() ?? '';
        user.password = user?.password?.trim() ?? '';
        user.confirmPassword = user?.confirmPassword?.trim() ?? '';

        if (!user.name || !user.email || !user.password || !user.confirmPassword) {
            Alert.alert('Error', 'Please fill all fields', [{ text: 'OK' }]);
            return;
        }
        if (user.password !== user.confirmPassword) {
            Alert.alert('Error', 'Passwords do not match', [{ text: 'OK' }]);
            return;
        }
        if (user.password.length < 6) {
            Alert.alert('Error', 'Password must be at least 6 characters', [{ text: 'OK' }]);
            return;
        }
        if (!emailRegex.test(user.email)) {
            Alert.alert('Error', 'Please enter valid email', [{ text: 'OK' }]);
            return;
        }
        try {
            await signUp(user);
            setUser(new SignUpUser({}));
            Alert.alert('Success', 'Signed Up successfully', [{ text: 'OK' }]);
            navigation.navigate('SignIn');
        } catch (error) {
            const err = JSON.parse(JSON.stringify(error));
            let errMsg = 'Error while sign up, please try again';
            if (err?.response?.status === 400) {
                errMsg = 'User already exists, please try with different email';
            }
            Alert.alert('Error', errMsg, [{ text: 'OK' }]);
        }
    };

    return (
        <View
            style={[
                styles.container,
                {
                    backgroundColor: darkMode ? '#333' : '#EDEADE',
                },
            ]}
        >
            <View style={styles.imageContainer}>
                <ImageBackground source={require('../../assets/download.jpeg')} style={styles.image}></ImageBackground>
            </View>
            <View style={styles.row}>
                <Text
                    style={[
                        styles.label,
                        {
                            color: darkMode ? '#fff' : '#333',
                        },
                    ]}
                >
                    Name
                </Text>
                <TextInput
                    style={[
                        styles.input,
                        focusedInput === 'name' && styles.inputFocused,
                        {
                            color: darkMode ? '#fff' : '#333',
                        },
                    ]}
                    value={user.name}
                    onChangeText={(value) => handleUserChange(value, 'name')}
                    placeholder="Enter name"
                    onBlur={() => setFocusedInput(null)}
                    onFocus={() => setFocusedInput('name')}
                />
            </View>
            <View style={styles.row}>
                <Text
                    style={[
                        styles.label,
                        {
                            color: darkMode ? '#fff' : '#333',
                        },
                    ]}
                >
                    Email
                </Text>
                <TextInput
                    style={[
                        styles.input,
                        focusedInput === 'email' && styles.inputFocused,
                        {
                            color: darkMode ? '#fff' : '#333',
                        },
                    ]}
                    value={user.email}
                    onChangeText={(value) => handleUserChange(value, 'email')}
                    placeholder="Enter email"
                    onFocus={() => setFocusedInput('email')}
                    onBlur={() => setFocusedInput(null)}
                />
            </View>
            <View style={styles.row}>
                <Text
                    style={[
                        styles.label,
                        {
                            color: darkMode ? '#fff' : '#333',
                        },
                    ]}
                >
                    Password
                </Text>
                <TextInput
                    style={[
                        styles.input,
                        focusedInput === 'password' && styles.inputFocused,
                        {
                            color: darkMode ? '#fff' : '#333',
                        },
                    ]}
                    value={user.password}
                    onChangeText={(value) => handleUserChange(value, 'password')}
                    placeholder="Enter password"
                    secureTextEntry
                    onFocus={() => setFocusedInput('password')}
                    onBlur={() => setFocusedInput(null)}
                />
            </View>
            <View style={styles.row}>
                <Text
                    style={[
                        styles.label,
                        {
                            color: darkMode ? '#fff' : '#333',
                        },
                    ]}
                >
                    Confirm password
                </Text>
                <TextInput
                    style={[
                        styles.input,
                        focusedInput === 'confirmPassword' && styles.inputFocused,
                        {
                            color: darkMode ? '#fff' : '#333',
                        },
                    ]}
                    value={user.confirmPassword}
                    onChangeText={(value) => handleUserChange(value, 'confirmPassword')}
                    placeholder="Enter confirm password"
                    secureTextEntry
                    onFocus={() => setFocusedInput('confirmPassword')}
                    onBlur={() => setFocusedInput(null)}
                />
            </View>
            <Button bgColor={'#007BFF'} text="Sign Up" btnFunction={handleSignUp} />
        </View>
    );
};

const styles = StyleSheet.create({
    container: {
        flex: 1,
        padding: 20,
    },
    row: {
        flexDirection: 'row',
        alignItems: 'center',
        marginBottom: 10,
    },
    label: {
        fontSize: 16,
        fontWeight: 'bold',
        color: '#333',
        width: 120,
    },
    input: {
        flex: 1,
        padding: 10,
        borderWidth: 1,
        borderColor: '#ccc',
        borderRadius: 8,
        backgroundColor: '#fff',
    },
    inputFocused: {
        borderColor: '#007BFF',
        shadowColor: '#007BFF',
        shadowOffset: { width: 0, height: 2 },
        shadowOpacity: 0.8,
        shadowRadius: 8,
        elevation: 5,
    },
    button: {
        padding: 10,
        backgroundColor: '#007BFF',
        borderRadius: 8,
        alignItems: 'center',
        marginTop: 20,
    },
    buttonText: {
        color: '#fff',
    },
    image: {
        justifyContent: 'center',
        height: 120,
        width: 120,
        alignSelf: 'center',
    },
    imageContainer: {
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 1 },
        shadowOpacity: 0.8,
        elevation: 5,
        overflow: 'hidden',
        width: 120,
        borderRadius: 8,
        marginBottom: 20,
        marginHorizontal: 'auto',
    },
});

export default SignUp;
