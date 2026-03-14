import React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import MaterialCommunityIcons from 'react-native-vector-icons/MaterialCommunityIcons';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';

import Todos from './src/pages/todos';
import User from './src/pages/user';
import SignIn from './src/pages/signin';
import SignUp from './src/pages/signup';
import useAuthStore from './src/store/auth';
import AddTodo from './src/pages/addTodo';

const bgColor = '#EDEADE';
const Tab = createBottomTabNavigator();

export default function App() {
    const { isLoggedIn } = useAuthStore();
    return (
        <NavigationContainer>
            <Tab.Navigator
                screenOptions={{
                    tabBarActiveTintColor: '#e91e63',
                    tabBarLabelStyle: {
                        fontSize: 12,
                    },
                    tabBarStyle: {
                        backgroundColor: 'white',
                    },
                    tabBarIconStyle: {
                        marginBottom: -3,
                    },
                }}
            >
                {isLoggedIn ? (
                    <>
                        <Tab.Screen
                            name="Todos"
                            component={Todos}
                            options={{
                                tabBarLabel: 'Todos',
                                tabBarIcon: ({ color, size }) => (
                                    <MaterialCommunityIcons
                                        name="clipboard-list"
                                        color={color}
                                        size={size}
                                    />
                                ),
                            }}
                        />
                        <Tab.Screen
                            name="Add"
                            component={AddTodo}
                            options={{
                                tabBarLabel: 'Add',
                                tabBarIcon: ({ color, size }) => (
                                    <MaterialCommunityIcons
                                        name="clipboard-plus-outline"
                                        color={color}
                                        size={size}
                                    />
                                ),
                            }}
                        />
                        <Tab.Screen
                            name="User"
                            component={User}
                            options={{
                                tabBarLabel: 'User',
                                tabBarIcon: ({ color, size }) => (
                                    <MaterialCommunityIcons
                                        name="face-man-profile"
                                        color={color}
                                        size={size}
                                    />
                                ),
                            }}
                        />
                    </>
                ) : (
                    <>
                        <Tab.Screen
                            name="SignIn"
                            component={SignIn}
                            options={{
                                tabBarLabel: 'Sign In',
                                tabBarIcon: ({ color, size }) => (
                                    <MaterialCommunityIcons
                                        name="account-circle"
                                        color={color}
                                        size={size}
                                    />
                                ),
                            }}
                        />
                        <Tab.Screen
                            name="SignUp"
                            component={SignUp}
                            options={{
                                tabBarLabel: 'Sign Up',
                                tabBarIcon: ({ color, size }) => (
                                    <MaterialCommunityIcons
                                        name="account-circle-outline"
                                        color={color}
                                        size={size}
                                    />
                                ),
                            }}
                        />
                    </>
                )}
            </Tab.Navigator>
        </NavigationContainer>
    );
}
