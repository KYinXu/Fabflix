import React, { useState } from 'react';
import type { Genre } from "@/types/types";

interface BrowseSectionProps {
    onBrowseTypeChange: (type: 'title' | 'genre') => void;
    onLetterChange: (letter: string) => void;
    onGenreChange: (genreId: number) => void;
    genres?: Genre[] | null;
}

const BrowseSection: React.FC<BrowseSectionProps> = ({ onBrowseTypeChange, onLetterChange, onGenreChange, genres }) => {
    const [activeTab, setActiveTab] = useState<'title' | 'genre'>('title');
    const [selectedLetter, setSelectedLetter] = useState<string>('All');

    const handleTabChange = (tab: 'title' | 'genre') => {
        setActiveTab(tab);
        if (tab === 'title') {
            setSelectedLetter('All');
            onLetterChange('All');
        } else {
            setSelectedLetter('A');
            onLetterChange('A');
        }
        onBrowseTypeChange(tab);
    };

    const handleLetterClick = (letter: string) => {
        setSelectedLetter(letter);
        onLetterChange(letter);
    };

    const handleGenreClick = (genreId: number) => {
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
                    <div className="inline-flex bg-gray-100 rounded-lg p-1">
                        <button
                            onClick={() => handleTabChange('title')}
                            className={`px-6 py-3 rounded-lg font-semibold transition-all duration-200 ${
                                activeTab === 'title'
                                    ? 'bg-gradient-to-r from-blue-500 via-purple-500 to-pink-500 text-white shadow-lg'
                                    : 'text-gray-600 hover:bg-gray-200'
                            }`}
                        >
                            Browse by Title
                        </button>
                        <button
                            onClick={() => handleTabChange('genre')}
                            className={`px-6 py-3 rounded-lg font-semibold transition-all duration-200 ${
                                activeTab === 'genre'
                                    ? 'bg-gradient-to-r from-blue-500 via-purple-500 to-pink-500 text-white shadow-lg'
                                    : 'text-gray-600 hover:bg-gray-200'
                            }`}
                        >
                            Browse by Genre
                        </button>
                    </div>
                </div>

                {/* Conditional Content */}
                {activeTab === 'title' ? (
                    // A-Z Browse Bar for Title Tab
                    <div className="bg-white rounded-lg shadow-md p-4">
                        <div className="text-center mb-3">
                            <p className="text-gray-600 text-sm font-medium">
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
                                            ? 'bg-gradient-to-r from-blue-500 via-purple-500 to-pink-500 text-white shadow-lg scale-110'
                                            : 'bg-gray-100 text-gray-700 hover:bg-gray-200 hover:scale-105'
                                    }`}
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
                                            ? 'bg-gradient-to-r from-blue-500 via-purple-500 to-pink-500 text-white shadow-lg scale-110'
                                            : 'bg-gray-100 text-gray-700 hover:bg-gray-200 hover:scale-105'
                                    }`}
                                >
                                    {number}
                                </button>
                            ))}
                            <button
                                onClick={() => handleLetterClick('All')}
                                className={`px-4 h-10 rounded-lg font-semibold transition-all duration-200 ${
                                    selectedLetter === 'All'
                                        ? 'bg-gradient-to-r from-blue-500 via-purple-500 to-pink-500 text-white shadow-lg scale-110'
                                        : 'bg-gray-100 text-gray-700 hover:bg-gray-200 hover:scale-105'
                                }`}
                            >
                                All
                            </button>
                        </div>
                    </div>
                ) : (
                    // Genre List for Genre Tab
                    <div className="bg-white rounded-lg shadow-md p-4">
                        <div className="text-center mb-4">
                            <p className="text-gray-600 text-sm font-medium">
                                Select a genre to browse movies
                            </p>
                        </div>
                        {genres && genres.length > 0 ? (
                            <div className="flex flex-wrap gap-3">
                                {genres.map((genre) => (
                                    <button
                                        key={genre.id}
                                        onClick={() => handleGenreClick(genre.id)}
                                        className="px-4 py-2 bg-gradient-to-r from-blue-500 via-purple-500 to-pink-500 text-white rounded-lg font-semibold hover:shadow-lg transition-all duration-200 hover:scale-105"
                                    >
                                        {genre.name}
                                    </button>
                                ))}
                            </div>
                        ) : (
                            <p className="text-gray-500 text-center">No genres available</p>
                        )}
                    </div>
                )}
            </div>
        </div>
    );
};

export default BrowseSection;
