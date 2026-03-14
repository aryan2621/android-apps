import React, { useEffect, useState } from 'react';
import {
    View,
    Text,
    StyleSheet,
    ActivityIndicator,
    ScrollView,
    RefreshControl,
    TouchableOpacity,
    Image,
} from 'react-native';
import { DBUser } from '../model';
import { getUser, updateUser } from '../service/user';
import useAuthStore from '../store/auth';
import Button from '../components/button';
import * as DocumentPicker from 'expo-document-picker';
import MaterialCommunityIcons from 'react-native-vector-icons/MaterialCommunityIcons';
import { fetchImage, uploadProfile } from '../config/firebase';

const User = () => {
    const { logout, currentUser, removeCurrentUser, setCurrentUser } = useAuthStore();

    const [user, setUser] = useState<DBUser>(new DBUser({}));
    const [loading, setLoading] = useState(true);
    const [refreshing, setRefreshing] = useState(false);
    const [image, setImage] = useState<string>('');
    const [uploading, setUploading] = useState(false);

    useEffect(() => {
        fetchUser();
    }, []);

    const fetchUser = async () => {
        setLoading(true);
        let response: DBUser = currentUser;
        if (!currentUser.email) {
            response = await getUser() as DBUser;
            setUser(new DBUser(response));
            setCurrentUser(response);
            if (response.imageUrl) {
                await fetchAndSetImage(response.imageUrl);
            }
        } else {
            const userData = currentUser;
            setUser(userData);
            if (userData.imageUrl) {
                setImage(userData.imageUrl);
            }

        }
        setLoading(false);
    };

    const fetchAndSetImage = async (imageUrl: string) => {
        if (!imageUrl) {
            setImage('');
            return;
        }
        const response = await fetchImage(imageUrl);
        setImage(response);
    };

    const onRefresh = async () => {
        setRefreshing(true);
        await fetchUser();
        setRefreshing(false);
    };

    const handleLogout = async () => {
        await logout();
        removeCurrentUser();
    };

    const handleUpload = async () => {
        const result = await DocumentPicker.getDocumentAsync({
            type: 'image/*',
            multiple: false,
            copyToCacheDirectory: true,
        });

        if (!result.canceled) {
            setUploading(true);
            const uri = result.assets[0].uri;
            setImage(uri);
            const uploadResult = await uploadProfile(uri);

            await Promise.all([updateUser(uploadResult), fetchAndSetImage(uploadResult)]);

            setCurrentUser({ ...user, imageUrl: uri });
            setUploading(false);
        }
    };

    const handleDeleteImage = async () => {
        setUploading(true);
        await updateUser('');
        setImage('');
        setCurrentUser({ ...user, imageUrl: '' });
        setUploading(false);
    };

    if (loading) {
        return (
            <View style={styles.loaderContainer}>
                <ActivityIndicator size="large" color="blue" />
            </View>
        );
    }

    return (
        <View style={[styles.container, { backgroundColor: '#EDEADE' }]}>
            <ScrollView
                contentContainerStyle={styles.scrollView}
                refreshControl={<RefreshControl refreshing={refreshing} onRefresh={onRefresh} />}
            >
                {uploading ? (
                    <ActivityIndicator size="large" color="#0000ff" />
                ) : (
                    <>
                        {image && (
                            <View style={styles.imageContainer}>
                                <Image source={{ uri: image }} style={styles.image} />
                                <TouchableOpacity
                                    style={styles.deleteButton}
                                    onPress={handleDeleteImage}
                                >
                                    <MaterialCommunityIcons name="delete" color={'red'} size={20} />
                                </TouchableOpacity>
                            </View>
                        )}
                        <TouchableOpacity style={styles.uploadButton} onPress={handleUpload}>
                            <MaterialCommunityIcons name="cloud-upload" color={'black'} size={20} />
                        </TouchableOpacity>
                    </>
                )}
                <Text style={styles.refreshMessage}>Pull to see changes</Text>
                <View style={styles.row}>
                    <Text style={styles.label}>Name:</Text>
                    <Text style={styles.text}>{user.name}</Text>
                </View>
                <View style={styles.row}>
                    <Text style={styles.label}>Email:</Text>
                    <Text style={styles.text}>{user.email}</Text>
                </View>
                <Button bgColor={'red'} text="Log Out" btnFunction={handleLogout} />
            </ScrollView>
        </View>
    );
};

const styles = StyleSheet.create({
    container: {
        flex: 1,
        paddingHorizontal: 20,
        paddingVertical: 0,
    },
    scrollView: {
        flexGrow: 1,
        justifyContent: 'center',
    },
    refreshMessage: {
        textAlign: 'center',
        marginVertical: 10,
        fontSize: 16,
        color: '#666',
    },
    row: {
        flexDirection: 'row',
        alignItems: 'center',
        marginBottom: 10,
    },
    label: {
        width: 100,
        fontSize: 18,
        fontWeight: 'bold',
        marginRight: 10,
    },
    text: {
        flex: 1,
        fontSize: 18,
    },
    imageContainer: {
        alignSelf: 'center',
        position: 'relative',
    },
    image: {
        width: 200,
        height: 200,
        borderRadius: 100,
        marginBottom: 10,
    },
    uploadButton: {
        alignSelf: 'center',
        marginBottom: 10,
        backgroundColor: 'white',
        height: 40,
        width: 40,
        borderRadius: 20,
        justifyContent: 'center',
        alignItems: 'center',
    },
    deleteButton: {
        position: 'absolute',
        top: 10,
        right: 10,
        backgroundColor: 'white',
        borderRadius: 20,
        padding: 5,
    },
    loaderContainer: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
    },
});

export default User;
