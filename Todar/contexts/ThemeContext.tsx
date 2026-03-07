import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import AsyncStorage from '@react-native-async-storage/async-storage';

export interface Theme {
  isDark: boolean;
  colors: {
    primary: string;
    primaryDark: string;
    primaryLight: string;
    
    background: string;
    surface: string;
    elevated: string;
    
    // Text colors
    text: string;
    textSecondary: string;
    textTertiary: string;
    textInverse: string;
    
    // Border and divider
    border: string;
    divider: string;
    
    // Semantic colors
    success: string;
    successLight: string;
    warning: string;
    warningLight: string;
    danger: string;
    dangerLight: string;
    info: string;
    infoLight: string;
    
    // UI element colors
    card: string;
    cardHover: string;
    shadow: string;
    overlay: string;
    
    // Interactive states
    disabled: string;
    disabledText: string;
    
    // Special colors
    streak: string;
    gold: string;
  };
  
  // Typography
  typography: {
    fontFamily: {
      regular: string;
      medium: string;
      bold: string;
      extraBold: string;
    };
    fontSize: {
      xs: number;
      sm: number;
      base: number;
      lg: number;
      xl: number;
      '2xl': number;
      '3xl': number;
      '4xl': number;
    };
  };
  
  // Spacing
  spacing: {
    xs: number;
    sm: number;
    md: number;
    lg: number;
    xl: number;
    '2xl': number;
    '3xl': number;
  };
  
  // Border radius
  radius: {
    sm: number;
    md: number;
    lg: number;
    xl: number;
    full: number;
  };
  
  // Shadows
  shadows: {
    sm: object;
    md: object;
    lg: object;
    xl: object;
  };
}

const lightTheme: Theme = {
  isDark: false,
  colors: {
    // Duolingo's signature green
    primary: '#58CC02',
    primaryDark: '#46A302',
    primaryLight: '#89E219',
    
    background: '#FFFFFF',
    surface: '#F7F7F7',
    elevated: '#FFFFFF',
    
    text: '#3C3C3C',
    textSecondary: '#777777',
    textTertiary: '#AFAFAF',
    textInverse: '#FFFFFF',
    
    border: '#E5E5E5',
    divider: '#E5E5E5',
    
    success: '#58CC02',
    successLight: '#D7FFB8',
    warning: '#FF9600',
    warningLight: '#FFC800',
    danger: '#FF4B4B',
    dangerLight: '#FFC1C1',
    info: '#1CB0F6',
    infoLight: '#84D8FF',
    
    card: '#FFFFFF',
    cardHover: '#F7F7F7',
    shadow: '#000000',
    overlay: 'rgba(0, 0, 0, 0.5)',
    
    disabled: '#E5E5E5',
    disabledText: '#AFAFAF',
    
    streak: '#FFC800',
    gold: '#FFC800',
  },
  
  typography: {
    fontFamily: {
      regular: 'System',
      medium: 'System',
      bold: 'System',
      extraBold: 'System',
    },
    fontSize: {
      xs: 11,
      sm: 13,
      base: 15,
      lg: 17,
      xl: 19,
      '2xl': 24,
      '3xl': 30,
      '4xl': 36,
    },
  },
  
  spacing: {
    xs: 4,
    sm: 8,
    md: 16,
    lg: 24,
    xl: 32,
    '2xl': 40,
    '3xl': 48,
  },
  
  radius: {
    sm: 8,
    md: 12,
    lg: 16,
    xl: 20,
    full: 9999,
  },
  
  shadows: {
    sm: {
      shadowColor: '#000',
      shadowOffset: { width: 0, height: 1 },
      shadowOpacity: 0.05,
      shadowRadius: 2,
      elevation: 1,
    },
    md: {
      shadowColor: '#000',
      shadowOffset: { width: 0, height: 2 },
      shadowOpacity: 0.08,
      shadowRadius: 4,
      elevation: 3,
    },
    lg: {
      shadowColor: '#000',
      shadowOffset: { width: 0, height: 4 },
      shadowOpacity: 0.12,
      shadowRadius: 8,
      elevation: 5,
    },
    xl: {
      shadowColor: '#000',
      shadowOffset: { width: 0, height: 6 },
      shadowOpacity: 0.16,
      shadowRadius: 12,
      elevation: 7,
    },
  },
};

const darkTheme: Theme = {
  ...lightTheme,
  isDark: true,
  colors: {
    primary: '#58CC02',
    primaryDark: '#46A302',
    primaryLight: '#6FDE08',
    
    background: '#131F24',
    surface: '#1E2D32',
    elevated: '#2A3A40',
    
    text: '#FFFFFF',
    textSecondary: '#AFAFAF',
    textTertiary: '#777777',
    textInverse: '#3C3C3C',
    
    border: '#374146',
    divider: '#374146',
    
    success: '#58CC02',
    successLight: '#2E6000',
    warning: '#FF9600',
    warningLight: '#804B00',
    danger: '#FF4B4B',
    dangerLight: '#802626',
    info: '#1CB0F6',
    infoLight: '#0E587B',
    
    card: '#1E2D32',
    cardHover: '#2A3A40',
    shadow: '#000000',
    overlay: 'rgba(0, 0, 0, 0.7)',
    
    disabled: '#374146',
    disabledText: '#777777',
    
    streak: '#FFC800',
    gold: '#FFC800',
  },
};

interface ThemeContextType {
  theme: Theme;
  toggleTheme: () => void;
  isDark: boolean;
}

const ThemeContext = createContext<ThemeContextType | undefined>(undefined);

const THEME_STORAGE_KEY = '@todar_theme';

export const ThemeProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [isDark, setIsDark] = useState(false);

  useEffect(() => {
    loadTheme();
  }, []);

  const loadTheme = async () => {
    try {
      const savedTheme = await AsyncStorage.getItem(THEME_STORAGE_KEY);
      if (savedTheme !== null) {
        setIsDark(JSON.parse(savedTheme));
      }
    } catch (error) {
      console.error('Error loading theme:', error);
    }
  };

  const saveTheme = async (darkMode: boolean) => {
    try {
      await AsyncStorage.setItem(THEME_STORAGE_KEY, JSON.stringify(darkMode));
    } catch (error) {
      console.error('Error saving theme:', error);
    }
  };

  const toggleTheme = () => {
    const newTheme = !isDark;
    setIsDark(newTheme);
    saveTheme(newTheme);
  };

  const theme = isDark ? darkTheme : lightTheme;

  return (
    <ThemeContext.Provider value={{ theme, toggleTheme, isDark }}>
      {children}
    </ThemeContext.Provider>
  );
};

export const useTheme = (): ThemeContextType => {
  const context = useContext(ThemeContext);
  if (context === undefined) {
    throw new Error('useTheme must be used within a ThemeProvider');
  }
  return context;
};