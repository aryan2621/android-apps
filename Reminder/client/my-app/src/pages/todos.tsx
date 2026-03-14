import React, { useEffect, useState } from 'react';
import {
    View,
    Text,
    StyleSheet,
    ActivityIndicator,
    RefreshControl,
    TouchableOpacity,
    Alert,
    SafeAreaView,
    Modal,
    TextInput,
} from 'react-native';
import { FlashList } from '@shopify/flash-list';
import { deleteTodoById, fetchTodos } from '../service/todo';
import { DBTask } from '../model';
import UpdateTodo from './updateTodo';
import Button from '../components/button';
import { formatTime } from '../utils';

const Todos = () => {
    const [todos, setTodos] = useState<DBTask[]>([]);
    const [nTodos, setNTodos] = useState<DBTask[]>([]);
    const [loading, setLoading] = useState(true);
    const [refreshing, setRefreshing] = useState(false);
    const [isModalVisible, setModalVisible] = useState(false);
    const [selectedTask, setSelectedTask] = useState<DBTask | null>(null);
    const [searchText, setSearchText] = useState('');


    const getAllTodos = async () => {
        const response = await fetchTodos();
        const todosData = response ?? [];
        const dbTodos: DBTask[] = (todosData ?? []).map((todo: any) => new DBTask(todo));
        setTodos(dbTodos);
        setNTodos(dbTodos);
    }

    useEffect(() => {
        const fetchTodosData = async () => {
            await getAllTodos();
            setLoading(false);
        };
        fetchTodosData();
    }, []);
    const onRefresh = async () => {
        setRefreshing(true);
        await getAllTodos();
        setRefreshing(false);
    };

    const onClick = (todo: DBTask) => {
        setSelectedTask(todo);
        setModalVisible(true);
    };

    const onLongPress = (todo: DBTask) => {
        Alert.alert(
            'Confirm Action',
            'Do you want to delete this task?',
            [
                {
                    text: 'Cancel',
                    style: 'cancel',
                },
                {
                    text: 'OK',
                    onPress: () => handleDelete(todo),
                },
            ],
            { cancelable: false }
        );
    };

    const handleDelete = async (todo: DBTask) => {
        if (todo.taskId) {
            try {
                await deleteTodoById(todo.taskId);
                setTodos(todos.filter((t) => t.taskId !== todo.taskId));
                setNTodos(nTodos.filter((t) => t.taskId !== todo.taskId));
            } catch (error) {
                const err = JSON.parse(JSON.stringify(error));
                let errMsg = 'Error while deleting task, please try again';
                if (err?.response?.status === 404) {
                    errMsg = 'Task not found';
                }
                Alert.alert('Error', errMsg, [{ text: 'OK' }]);
            }
        }
    };

    const handleSearchText = () => {
        const text = searchText.trim();
        if (!text) {
            return nTodos;
        }
        return nTodos.filter((todo) => {
            return todo?.title?.toLowerCase().includes(text.toLowerCase());
        });
    };

    const closeModal = () => {
        setModalVisible(false);
        setSelectedTask(null);
    };

    return (
        <SafeAreaView
            style={[
                styles.container,
                {
                    backgroundColor: '#EDEADE',
                },
            ]}
        >
            {loading ? (
                <ActivityIndicator size="large" color="blue" style={styles.loader} />
            ) : (
                <>
                    <View style={{ marginVertical: 10 }}>
                        <TextInput
                            style={styles.searchBox}
                            placeholder="Search by title"
                            onChangeText={(text) => {
                                setSearchText(text);
                            }}
                        />
                    </View>
                    <FlashList
                        data={nTodos.length > 0 ? handleSearchText() : todos}
                        keyExtractor={(item) => item.taskId?.toString() || Math.random().toString()}
                        renderItem={({ item }) => (
                            <TouchableOpacity
                                style={styles.taskContainer}
                                onPress={() => onClick(item)}
                                onLongPress={() => onLongPress(item)}
                            >
                                <Text style={styles.title}>{item.title}</Text>
                                <Text style={styles.description}>{item.description}</Text>
                                <View style={styles.statusContainer}>
                                    <Text
                                        style={[
                                            styles.statusLabel,
                                            item.done ? styles.statusDone : styles.statusNotDone,
                                        ]}
                                    >
                                        {item.done ? 'Done' : 'Not Done'}
                                    </Text>
                                    {item.createdAt && (
                                        <Text style={styles.createdAt}>
                                            {formatTime(item.createdAt)}
                                        </Text>
                                    )}
                                </View>
                            </TouchableOpacity>
                        )}
                        estimatedItemSize={150}
                        refreshControl={
                            <RefreshControl refreshing={refreshing} onRefresh={onRefresh} />
                        }
                        ListHeaderComponent={
                            <View style={styles.header}>
                                <Text style={styles.headerText}>Pull down to refresh</Text>
                            </View>
                        }
                    />
                </>
            )}

            <Modal visible={isModalVisible} onRequestClose={closeModal} style={styles.modal}>
                <UpdateTodo task={selectedTask} />
                <View style={{ padding: 20 }}>
                    <Button bgColor={'#007BFF'} text="Close" btnFunction={closeModal} />
                </View>
            </Modal>
        </SafeAreaView>
    );
};

const styles = StyleSheet.create({
    container: {
        flex: 1,
        paddingHorizontal: 20,
        paddingTop: 2,
    },
    taskContainer: {
        backgroundColor: '#fff',
        borderRadius: 4,
        padding: 10,
        marginBottom: 10,
        elevation: 3,
    },
    title: {
        fontSize: 18,
        fontWeight: 'bold',
        marginBottom: 5,
    },
    description: {
        fontSize: 16,
        color: '#666',
        marginBottom: 5,
    },
    statusContainer: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
    },
    statusLabel: {
        fontSize: 16,
        fontWeight: 'bold',
    },
    statusDone: {
        color: 'green',
    },
    statusNotDone: {
        color: 'red',
    },
    createdAt: {
        fontSize: 14,
        color: '#666',
    },
    loader: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
    },
    header: {
        paddingVertical: 10,
        alignItems: 'center',
    },
    headerText: {
        fontSize: 16,
        color: '#666',
    },
    modal: {
        justifyContent: 'center',
        alignItems: 'center',
        borderRadius: 20,
    },
    searchBox: {
        height: 40,
        borderColor: 'gray',
        borderWidth: 1,
        borderRadius: 5,
        paddingHorizontal: 10,
    },
});

export default Todos;
