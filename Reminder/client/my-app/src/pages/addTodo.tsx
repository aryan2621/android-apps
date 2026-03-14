import React, { useState, useRef, useEffect } from 'react';
import {
    View,
    Text,
    StyleSheet,
    TextInput,
    Switch,
    useColorScheme,
    Image,
    TouchableOpacity,
    ActivityIndicator,
    Alert,
} from 'react-native';
import { CameraView, useCameraPermissions } from 'expo-camera';
import { uploadImage } from '../config/firebase';
import { createTodo } from '../service/todo';
import Button from '../components/button';
import { DBTask } from '../model';
import * as DocumentPicker from 'expo-document-picker';

enum Facing {
    Front = 'front',
    Back = 'back',
}

enum Mode {
    Picture = 'picture',
}

const AddTodo = ({ navigation }: any) => {
    const darkMode = useColorScheme() === 'dark';

    const [todo, setTodo] = useState(new DBTask({}));
    const [focusedInput, setFocusedInput] = useState<string | null>(null);
    const [cameraPermission, setCameraPermission] = useCameraPermissions();
    const [facing, setFacing] = useState(Facing.Back);
    const [image, setImage] = useState<string>(todo.imageUrl ?? '');
    const [openCamera, setOpenCamera] = useState<boolean>(false);
    const [isUploadingImage, setIsUploadingImage] = useState(false);
    const cameraViewRef = useRef<CameraView>(null);

    if (!cameraPermission) {
        return <Text>Camera permission not granted</Text>;
    }

    if (!cameraPermission.granted) {
        return (
            <View
                style={[
                    styles.container,
                    {
                        backgroundColor: darkMode ? '#333' : '#f9f9f9',
                    },
                ]}
            >
                <Text style={styles.permissionText}>
                    We need your permission to show the camera
                </Text>
                <Button
                    text="Grant camera permission"
                    bgColor="#007BFF"
                    btnFunction={setCameraPermission}
                />
            </View>
        );
    }

    const toggleFacing = () => {
        setFacing(facing === Facing.Back ? Facing.Front : Facing.Back);
    };

    const toggleOpenCamera = () => {
        setOpenCamera(!openCamera);
    };

    const captureImage = async () => {
        try {
            const image = await cameraViewRef.current?.takePictureAsync({
                quality: 0.5,
            });
            toggleOpenCamera();
            if (image) {
                setImage(image.uri);
                setIsUploadingImage(true);
                const url = await uploadImage(image.uri);
                setTodo({
                    ...todo,
                    imageUrl: url,
                } as any);
                Alert.alert('Success', 'Image captured successfully', [{ text: 'OK' }]);
            } else {
                Alert.alert('Error', `Image is not present, please try again`, [{ text: 'OK' }]);
            }
        } catch (error) {
            Alert.alert('Error', `Failed to capture image,please try again`, [{ text: 'OK' }]);
        }
        setIsUploadingImage(false);
    };

    const handleTaskChanges = (value: any, name: any) => {
        setTodo({
            ...todo,
            [name]: value,
        });
    };
    const handleSave = async () => {
        todo.title = todo?.title?.trim() ?? '';
        todo.description = todo.description ?? '';
        if (todo.title === '' || todo.description === '') {
            Alert.alert('Error', 'Title and Description are required', [{ text: 'OK' }]);
            return;
        }
        if (todo.title.length < 3 || todo.title.length > 20) {
            Alert.alert('Error', 'Title should be between 3 and 20 characters', [{ text: 'OK' }]);
            return;
        }
        todo.done = todo.done ?? false;
        todo.imageUrl = todo.imageUrl ?? '';
        try {
            await createTodo(todo);
            setTodo(new DBTask({}));
            setImage('');
            Alert.alert('Success', 'Task created successfully', [{ text: 'OK' }]);
            navigation.navigate('Todos');
        } catch (error) {
            Alert.alert('Error', 'Failed to create task, please try again', [{ text: 'OK' }]);
        }
    };
    const handleUpload = async () => {
        if (openCamera) {
            toggleOpenCamera();
        }
        try {
            const result = await DocumentPicker.getDocumentAsync({
                type: 'image/*',
                copyToCacheDirectory: true,
            });
            if (!result.canceled) {
                const uri = result.assets[0].uri;
                setImage(uri);
                setIsUploadingImage(true);
                const url = await uploadImage(uri);
                setTodo({
                    ...todo,
                    imageUrl: url,
                } as any);
                Alert.alert('Success', 'Image uploaded successfully', [{ text: 'OK' }]);
            } else {
                Alert.alert('Error', `Failed to upload image, please try again`, [{ text: 'OK' }]);
            }
        } catch (error) {
            Alert.alert('Error', `Failed to upload image,please try again`, [{ text: 'OK' }]);
        }
        setIsUploadingImage(false);
    }

    return (
        <View
            style={[
                styles.container,
                {
                    backgroundColor: darkMode ? '#333' : '#EDEADE',
                },
            ]}
        >
            <View style={styles.row}>
                <Text
                    style={[
                        styles.label,
                        {
                            color: darkMode ? '#fff' : '#333',
                        },
                    ]}
                >
                    Title
                </Text>
                <TextInput
                    style={[
                        styles.input,
                        focusedInput === 'title' && styles.inputFocused,
                        {
                            color: darkMode ? '#fff' : '#333',
                        },
                    ]}
                    value={todo.title}
                    onChangeText={(value) => handleTaskChanges(value, 'title')}
                    placeholder="Enter title"
                    onFocus={() => setFocusedInput('title')}
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
                    Description
                </Text>
                <TextInput
                    style={[
                        styles.input,
                        focusedInput === 'description' && styles.inputFocused,
                        {
                            color: darkMode ? '#fff' : '#333',
                        },
                    ]}
                    value={todo.description}
                    onChangeText={(value) => handleTaskChanges(value, 'description')}
                    placeholder="Enter description"
                    multiline
                    onFocus={() => setFocusedInput('description')}
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
                    Done
                </Text>
                <Switch
                    value={todo.done}
                    onValueChange={(value) => handleTaskChanges(value, 'done')}
                />
            </View>
            <>
                {!openCamera ? (
                    <>
                        <View style={styles.row}>
                            <Text
                                style={[
                                    styles.label,
                                    {
                                        color: darkMode ? '#fff' : '#333',
                                    },
                                ]}
                            >
                                Image
                            </Text>
                            {image ? (
                                <>
                                    {isUploadingImage ? (
                                        <ActivityIndicator size="large" color="#0000ff" />
                                    ) : (
                                        <View>
                                            <Image source={{ uri: image }} style={styles.image} />
                                            <Button
                                                text="Delete"
                                                bgColor="#007BFF"
                                                btnFunction={() => setImage('')}
                                            />
                                        </View>
                                    )}
                                </>
                            ) : (
                                <></>
                            )}
                        </View>
                    </>
                ) : (
                    <CameraView
                        ref={cameraViewRef}
                        style={styles.camera}
                        facing={facing}
                        mode={Mode.Picture}
                    >
                        <View style={styles.buttonContainer}>
                            <TouchableOpacity style={styles.cameraButton} onPress={captureImage}>
                                <Text style={styles.buttonText}>Capture</Text>
                            </TouchableOpacity>
                            <TouchableOpacity style={styles.cameraButton} onPress={toggleFacing}>
                                <Text style={styles.buttonText}>Flip</Text>
                            </TouchableOpacity>
                        </View>
                    </CameraView>
                )}
                <View style={styles.btnContainer}>
                    <Button
                        bgColor="#007BFF"
                        text={openCamera ? 'Close Camera' : 'Open Camera'}
                        btnFunction={toggleOpenCamera}
                    />
                    <Button
                        bgColor={'#007BFF'}
                        btnFunction={handleUpload}
                        text={'Upload Image'}
                    />
                </View>
                <Button bgColor="#007BFF" text="Save" btnFunction={handleSave} />
            </>
        </View>
    );
};

const styles = StyleSheet.create({
    btnContainer: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        marginVertical: 10,
    },
    container: {
        flex: 1,
        backgroundColor: '#EDEADE',
        padding: 20,
    },
    row: {
        flexDirection: 'row',
        alignItems: 'center',
        marginBottom: 15,
    },
    label: {
        fontSize: 16,
        fontWeight: 'bold',
        color: '#333',
        width: 100, // Adjust this value as needed
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
    camera: {
        width: '100%',
        height: 360,
        borderRadius: 8,
    },
    buttonContainer: {
        flex: 1,
        flexDirection: 'row',
        justifyContent: 'space-around',
        alignItems: 'flex-end',
        paddingBottom: 20,
        paddingHorizontal: 20,
        backgroundColor: 'rgba(0, 0, 0, 0.3)', // Adds a slight background to the button area for better visibility
    },
    cameraButton: {
        padding: 10,
        borderRadius: 5,
        borderWidth: 1,
        borderColor: '#fff',
        width: 80,
        alignItems: 'center',
    },
    button: {
        padding: 10,
        borderRadius: 4,
        backgroundColor: '#007BFF',
        height: 40,
        width: '100%',
        marginTop: 10,
    },
    buttonText: {
        color: '#fff',
        textAlign: 'center',
    },
    image: {
        width: 150,
        height: 150,
    },
    permissionText: {
        color: '#333',
        fontSize: 16,
        marginBottom: 10,
    },
});

export default AddTodo;
