import React, { useState } from "react";
import { useFetchAddStar } from "../hooks/useFetchAddStars";
import { useFetchAddMovie } from "../hooks/useFetchAddMovies";
import { useFetchMetadata } from "../hooks/useFetchMetadata";

const Dashboard: React.FC = () => {
    // Hooks
    const { data: starData, loading: starLoading, error: starError, fetchAddStar } = useFetchAddStar();
    const { data: movieData, loading: movieLoading, error: movieError, fetchAddMovie } = useFetchAddMovie();
    const { data: metadata, loading: metaLoading, error: metaError, fetchMetadata } = useFetchMetadata();

    // Form State
    const [starName, setStarName] = useState("");
    const [birthYear, setBirthYear] = useState("");
    const [title, setTitle] = useState("");
    const [year, setYear] = useState("");
    const [director, setDirector] = useState("");
    const [star, setStar] = useState("");
    const [genre, setGenre] = useState("");
    const [showMetadata, setShowMetadata] = useState(false);

    // Handlers
    const handleAddStar = async (e: React.FormEvent) => {
        e.preventDefault();
        await fetchAddStar(starName, birthYear || null);
    };

    const handleAddMovie = async (e: React.FormEvent) => {
        e.preventDefault();
        await fetchAddMovie(title, year, director, star, genre);
    };

    const handleViewMetadata = async () => {
        setShowMetadata(true);
        await fetchMetadata();
    };

    return (
        <div className="flex flex-col items-center min-h-screen p-10"
             style={{ backgroundColor: 'var(--theme-bg-primary)' }}>
            <div className="p-10 rounded-xl shadow-lg w-full max-w-4xl border-2"
                 style={{
                     backgroundColor: 'var(--theme-bg-card)',
                     borderColor: 'var(--theme-border-secondary)',
                     boxShadow: '0 10px 25px rgba(0, 0, 0, 0.3)'
                 }}>
                <h1 className="text-4xl font-bold text-center mb-10"
                    style={{
                        background: 'linear-gradient(to right, var(--theme-primary-light), var(--theme-secondary-light), var(--theme-accent-light))',
                        WebkitBackgroundClip: 'text',
                        WebkitTextFillColor: 'transparent',
                        backgroundClip: 'text'
                    }}>
                    Employee Dashboard
                </h1>

                {/* --- View Metadata Button --- */}
                <div className="text-center mb-12">
                    <button
                        onClick={handleViewMetadata}
                        disabled={metaLoading}
                        className="bg-blue-600 text-white px-6 py-3 rounded-lg font-semibold transition-all hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                        {metaLoading ? "Loading Metadata..." : "View Database Metadata"}
                    </button>

                    {metaError && (
                        <p className="mt-4 text-red-500 text-sm font-medium">{metaError}</p>
                    )}

                    {showMetadata && metadata && (
                        <div className="mt-10 bg-white p-6 rounded-lg shadow-md max-h-[400px] overflow-y-auto">
                            <h2 className="text-2xl font-semibold mb-4 text-gray-800">Database Tables</h2>
                            {metadata.tables.map((table) => (
                                <div key={table.name} className="mb-6">
                                    <h3 className="text-lg font-bold text-gray-700 mb-2">{table.name}</h3>
                                    <table className="w-full border border-gray-300 text-left">
                                        <thead className="bg-gray-100">
                                        <tr>
                                            <th className="border border-gray-300 p-2">Column Name</th>
                                            <th className="border border-gray-300 p-2">Type</th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                        {table.columns.map((col) => (
                                            <tr key={col.name}>
                                                <td className="border border-gray-300 p-2">{col.name}</td>
                                                <td className="border border-gray-300 p-2">{col.type}</td>
                                            </tr>
                                        ))}
                                        </tbody>
                                    </table>
                                </div>
                            ))}
                        </div>
                    )}
                </div>

                {/* --- Add Star Form --- */}
                <section className="mb-12">
                    <h2 className="text-2xl font-semibold mb-6 text-center"
                        style={{ color: 'var(--theme-text-primary)' }}>Add Star</h2>

                    <form onSubmit={handleAddStar} className="space-y-6">
                        <div>
                            <label className="block text-sm font-medium mb-2"
                                   style={{ color: 'var(--theme-text-primary)' }}>Star Name</label>
                            <input
                                type="text"
                                value={starName}
                                onChange={(e) => setStarName(e.target.value)}
                                required
                                className="w-full p-3 rounded-lg border-2 focus:outline-none focus:ring-2 transition-all duration-200"
                                style={{
                                    borderColor: 'var(--theme-border-secondary)',
                                    backgroundColor: 'var(--theme-bg-primary)',
                                    color: 'var(--theme-text-primary)',
                                    '--tw-ring-color': 'var(--theme-primary)'
                                } as React.CSSProperties}
                            />
                        </div>

                        <div>
                            <label className="block text-sm font-medium mb-2"
                                   style={{ color: 'var(--theme-text-primary)' }}>Birth Year (Optional)</label>
                            <input
                                type="number"
                                value={birthYear}
                                onChange={(e) => setBirthYear(e.target.value)}
                                className="w-full p-3 rounded-lg border-2 focus:outline-none focus:ring-2 transition-all duration-200"
                                style={{
                                    borderColor: 'var(--theme-border-secondary)',
                                    backgroundColor: 'var(--theme-bg-primary)',
                                    color: 'var(--theme-text-primary)',
                                    '--tw-ring-color': 'var(--theme-primary)'
                                } as React.CSSProperties}
                            />
                        </div>

                        <button
                            type="submit"
                            disabled={starLoading}
                            className="w-full py-3 rounded-lg font-semibold transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
                            style={{
                                backgroundColor: 'var(--theme-primary)',
                                color: 'white',
                                boxShadow: '0 4px 12px rgba(0, 0, 0, 0.15)'
                            }}
                        >
                            {starLoading ? "Adding..." : "Add Star"}
                        </button>
                    </form>

                    {starError && (
                        <div className="mt-6 p-4 rounded-lg border-2 flex items-center gap-3"
                             style={{
                                 backgroundColor: 'rgba(220, 38, 38, 0.1)',
                                 borderColor: 'var(--theme-error)',
                                 color: 'var(--theme-error)'
                             }}>
                            <span className="text-sm font-medium">{starError}</span>
                        </div>
                    )}
                    {starData && starData.status === "success" && (
                        <div className="mt-6 p-4 rounded-lg border-2 flex items-center gap-3"
                             style={{
                                 backgroundColor: 'rgba(34, 197, 94, 0.1)',
                                 borderColor: 'var(--theme-success)',
                                 color: 'var(--theme-success)'
                             }}>
                            <span className="text-sm font-medium">Star successfully added!</span>
                        </div>
                    )}
                </section>

                {/* --- Add Movie Form --- */}
                <section>
                    <h2 className="text-2xl font-semibold mb-6 text-center"
                        style={{ color: 'var(--theme-text-primary)' }}>Add Movie</h2>

                    <form onSubmit={handleAddMovie} className="space-y-6">
                        <div>
                            <label className="block text-sm font-medium mb-2"
                                   style={{ color: 'var(--theme-text-primary)' }}>Title</label>
                            <input
                                type="text"
                                value={title}
                                onChange={(e) => setTitle(e.target.value)}
                                required
                                className="w-full p-3 rounded-lg border-2 focus:outline-none focus:ring-2 transition-all duration-200"
                                style={{
                                    borderColor: 'var(--theme-border-secondary)',
                                    backgroundColor: 'var(--theme-bg-primary)',
                                    color: 'var(--theme-text-primary)',
                                } as React.CSSProperties}
                            />
                        </div>

                        <div className="grid grid-cols-2 gap-6">
                            <div>
                                <label className="block text-sm font-medium mb-2"
                                       style={{ color: 'var(--theme-text-primary)' }}>Year</label>
                                <input
                                    type="number"
                                    value={year}
                                    onChange={(e) => setYear(e.target.value)}
                                    required
                                    className="w-full p-3 rounded-lg border-2 focus:outline-none focus:ring-2 transition-all duration-200"
                                    style={{
                                        borderColor: 'var(--theme-border-secondary)',
                                        backgroundColor: 'var(--theme-bg-primary)',
                                        color: 'var(--theme-text-primary)',
                                    } as React.CSSProperties}
                                />
                            </div>

                            <div>
                                <label className="block text-sm font-medium mb-2"
                                       style={{ color: 'var(--theme-text-primary)' }}>Director</label>
                                <input
                                    type="text"
                                    value={director}
                                    onChange={(e) => setDirector(e.target.value)}
                                    required
                                    className="w-full p-3 rounded-lg border-2 focus:outline-none focus:ring-2 transition-all duration-200"
                                    style={{
                                        borderColor: 'var(--theme-border-secondary)',
                                        backgroundColor: 'var(--theme-bg-primary)',
                                        color: 'var(--theme-text-primary)',
                                    } as React.CSSProperties}
                                />
                            </div>
                        </div>

                        <div className="grid grid-cols-2 gap-6">
                            <div>
                                <label className="block text-sm font-medium mb-2"
                                       style={{ color: 'var(--theme-text-primary)' }}>Star</label>
                                <input
                                    type="text"
                                    value={star}
                                    onChange={(e) => setStar(e.target.value)}
                                    required
                                    className="w-full p-3 rounded-lg border-2 focus:outline-none focus:ring-2 transition-all duration-200"
                                    style={{
                                        borderColor: 'var(--theme-border-secondary)',
                                        backgroundColor: 'var(--theme-bg-primary)',
                                        color: 'var(--theme-text-primary)',
                                    } as React.CSSProperties}
                                />
                            </div>

                            <div>
                                <label className="block text-sm font-medium mb-2"
                                       style={{ color: 'var(--theme-text-primary)' }}>Genre</label>
                                <input
                                    type="text"
                                    value={genre}
                                    onChange={(e) => setGenre(e.target.value)}
                                    required
                                    className="w-full p-3 rounded-lg border-2 focus:outline-none focus:ring-2 transition-all duration-200"
                                    style={{
                                        borderColor: 'var(--theme-border-secondary)',
                                        backgroundColor: 'var(--theme-bg-primary)',
                                        color: 'var(--theme-text-primary)',
                                    } as React.CSSProperties}
                                />
                            </div>
                        </div>

                        <button
                            type="submit"
                            disabled={movieLoading}
                            className="w-full py-3 rounded-lg font-semibold transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
                            style={{
                                backgroundColor: 'var(--theme-primary)',
                                color: 'white',
                                boxShadow: '0 4px 12px rgba(0, 0, 0, 0.15)'
                            }}
                        >
                            {movieLoading ? "Adding..." : "Add Movie"}
                        </button>
                    </form>

                    {movieError && (
                        <div className="mt-6 p-4 rounded-lg border-2 flex items-center gap-3"
                             style={{
                                 backgroundColor: 'rgba(220, 38, 38, 0.1)',
                                 borderColor: 'var(--theme-error)',
                                 color: 'var(--theme-error)'
                             }}>
                            <span className="text-sm font-medium">{movieError}</span>
                        </div>
                    )}
                    {movieData && movieData.status === "success" && (
                        <div className="mt-6 p-4 rounded-lg border-2 flex items-center gap-3"
                             style={{
                                 backgroundColor: 'rgba(34, 197, 94, 0.1)',
                                 borderColor: 'var(--theme-success)',
                                 color: 'var(--theme-success)'
                             }}>
                            <span className="text-sm font-medium">Movie successfully added!</span>
                        </div>
                    )}
                </section>
            </div>
        </div>
    );
};

export default Dashboard;
