import React, { useState, useEffect } from 'react';
import type { Genre } from "@/types/types";

interface BrowseSectionProps {
    onBrowseTypeChange: (type: 'title' | 'genre') => void;
    onLetterChange: (letter: string) => void;
    onGenreChange: (genreId: number) => void;
    genres?: Genre[] | null;
    initialBrowseType?: 'title' | 'genre';
    initialSelectedLetter?: string;
    initialSelectedGenreId?: number | null;
}

const BrowseSection: React.FC<BrowseSectionProps> = ({
    onLetterChange, 
    onGenreChange, 
    genres,
    initialBrowseType,
    initialSelectedLetter,
    initialSelectedGenreId
}) => {
    const [activeTab, setActiveTab] = useState<'title' | 'genre'>(initialBrowseType || 'title');
    const [selectedLetter, setSelectedLetter] = useState<string>(initialSelectedLetter || 'All');
    const [selectedGenreId, setSelectedGenreId] = useState<number | null>(initialSelectedGenreId || null);

    // Sync internal state with props when they change
    useEffect(() => {
        if (initialBrowseType) {
            setActiveTab(initialBrowseType);
        }
    }, [initialBrowseType]);

    useEffect(() => {
        if (initialSelectedLetter) {
            setSelectedLetter(initialSelectedLetter);
        }
    }, [initialSelectedLetter]);

    useEffect(() => {
        if (initialSelectedGenreId !== undefined) {
            setSelectedGenreId(initialSelectedGenreId);
        }
    }, [initialSelectedGenreId]);

    const handleTabChange = (tab: 'title' | 'genre') => {
        setActiveTab(tab);
        if (tab === 'title') {
            setSelectedLetter('All');
            setSelectedGenreId(null);
        } else {
            setSelectedLetter('');
            setSelectedGenreId(null);
        }
        // Don't trigger queries immediately - only update the UI state
    };

    const handleLetterClick = (letter: string) => {
        setSelectedLetter(letter);
        onLetterChange(letter);
    };

    const handleGenreClick = (genreId: number) => {
        setSelectedGenreId(genreId);
        onGenreChange(genreId);
    };

    // Generate A-Z letters and 0-9 numbers
    const letters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'.split('');
    const numbers = '0123456789'.split('');

    return (
        <div className="container mx-auto px-4 mb-8">
            <div className="max-w-7xl mx-auto">
                {/* Tabs */}
                <div className="flex justify-center mb-6">
                    <div className="flex gap-8">
                        <button
                            onClick={() => handleTabChange('title')}
                            className={`px-4 py-2 font-semibold transition-all duration-200 ${
                                activeTab === 'title'
                                    ? 'text-white font-bold underline decoration-2 underline-offset-4'
                                    : 'text-gray-400 hover:text-white'
                            }`}
                        >
                            Browse by Title
                        </button>
                        <button
                            onClick={() => handleTabChange('genre')}
                            className={`px-4 py-2 font-semibold transition-all duration-200 ${
                                activeTab === 'genre'
                                    ? 'text-white font-bold underline decoration-2 underline-offset-4'
                                    : 'text-gray-400 hover:text-white'
                            }`}
                        >
                            Browse by Genre
                        </button>
                    </div>
                </div>

                {/* Conditional Content */}
                {activeTab === 'title' ? (
                    // A-Z Browse Bar for Title Tab
                    <div className="rounded-lg p-4" style={{ backgroundColor: 'var(--theme-bg-secondary)', borderColor: 'var(--theme-border-primary)', border: '1px solid' }}>
                        <div className="text-center mb-3">
                            <p className="text-sm font-medium" style={{ color: 'var(--theme-text-secondary)' }}>
                                Select a letter to browse movies
                            </p>
                        </div>
                        <div className="flex flex-wrap justify-center gap-2">
                            {letters.map((letter) => (
                                <button
                                    key={letter}
                                    onClick={() => handleLetterClick(letter)}
                                    className={`w-10 h-10 rounded-lg font-semibold transition-all duration-200 ${
                                        selectedLetter === letter
                                            ? 'text-white shadow-lg scale-110'
                                            : 'hover:scale-105'
                                    }`}
                                    style={{
                                        backgroundColor: selectedLetter === letter ? 'var(--theme-primary)' : 'var(--theme-bg-tertiary)',
                                        color: selectedLetter === letter ? 'var(--theme-text-primary)' : 'var(--theme-text-secondary)',
                                        borderColor: 'var(--theme-border-primary)',
                                        border: '1px solid'
                                    }}
                                >
                                    {letter}
                                </button>
                            ))}
                            {numbers.map((number) => (
                                <button
                                    key={number}
                                    onClick={() => handleLetterClick(number)}
                                    className={`w-10 h-10 rounded-lg font-semibold transition-all duration-200 ${
                                        selectedLetter === number
                                            ? 'text-white shadow-lg scale-110'
                                            : 'hover:scale-105'
                                    }`}
                                    style={{
                                        backgroundColor: selectedLetter === number ? 'var(--theme-primary)' : 'var(--theme-bg-tertiary)',
                                        color: selectedLetter === number ? 'var(--theme-text-primary)' : 'var(--theme-text-secondary)',
                                        borderColor: 'var(--theme-border-primary)',
                                        border: '1px solid'
                                    }}
                                >
                                    {number}
                                </button>
                            ))}
                            <button
                                onClick={() => handleLetterClick('All')}
                                className={`px-4 h-10 rounded-lg font-semibold transition-all duration-200 ${
                                    selectedLetter === 'All'
                                        ? 'text-white shadow-lg scale-110'
                                        : 'hover:scale-105'
                                }`}
                                style={{
                                    backgroundColor: selectedLetter === 'All' ? 'var(--theme-primary)' : 'var(--theme-bg-tertiary)',
                                    color: selectedLetter === 'All' ? 'var(--theme-text-primary)' : 'var(--theme-text-secondary)',
                                    borderColor: 'var(--theme-border-primary)',
                                    border: '1px solid'
                                }}
                            >
                                All
                            </button>
                        </div>
                    </div>
                ) : (
                    // Genre List for Genre Tab
                    <div className="rounded-lg p-4" style={{ backgroundColor: 'var(--theme-bg-secondary)', borderColor: 'var(--theme-border-primary)', border: '1px solid' }}>
                        <div className="text-center mb-4">
                            <p className="text-sm font-medium" style={{ color: 'var(--theme-text-secondary)' }}>
                                Select a genre to browse movies
                            </p>
                        </div>
                        {genres && genres.length > 0 ? (
                            <div className="flex flex-wrap gap-3">
                                {genres.map((genre) => (
                                    <button
                                        key={genre.id}
                                        onClick={() => handleGenreClick(genre.id)}
                                        className={`px-4 py-2 rounded-lg font-semibold transition-all duration-200 ${
                                            selectedGenreId === genre.id
                                                ? 'text-white shadow-lg scale-110'
                                                : 'hover:shadow-lg hover:scale-105'
                                        }`}
                                        style={{
                                            background: selectedGenreId === genre.id 
                                                ? 'linear-gradient(to right, var(--theme-primary-hover), var(--theme-secondary-hover), var(--theme-accent-hover))'
                                                : 'linear-gradient(to right, var(--theme-primary), var(--theme-secondary), var(--theme-accent))',
                                            color: 'white'
                                        }}
                                        onMouseEnter={(e) => {
                                            if (selectedGenreId !== genre.id) {
                                                const target = e.target as HTMLElement;
                                                target.style.background = 'linear-gradient(to right, var(--theme-primary-hover), var(--theme-secondary-hover), var(--theme-accent-hover))';
                                            }
                                        }}
                                        onMouseLeave={(e) => {
                                            if (selectedGenreId !== genre.id) {
                                                const target = e.target as HTMLElement;
                                                target.style.background = 'linear-gradient(to right, var(--theme-primary), var(--theme-secondary), var(--theme-accent))';
                                            }
                                        }}
                                    >
                                        {genre.name}
                                    </button>
                                ))}
                            </div>
                        ) : (
                            <p className="text-center" style={{ color: 'var(--theme-text-muted)' }}>No genres available</p>
                        )}
                    </div>
                )}
            </div>
        </div>
    );
};

export default BrowseSection;
