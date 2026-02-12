import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './App.css';
import UserList from './components/UserList';
import UserForm from './components/UserForm';

function App() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [editingUser, setEditingUser] = useState(null);

  const fetchUsers = async () => {
    try {
      setLoading(true);
      const response = await axios.get('/api/users');
      setUsers(response.data);
      setError(null);
    } catch (err) {
      setError('Failed to fetch users. Make sure the backend server is running.');
      console.error('Error fetching users:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, []);

  const handleCreateUser = async (user) => {
    try {
      await axios.post('/api/users', user);
      fetchUsers();
      return true;
    } catch (err) {
      console.error('Error creating user:', err);
      return false;
    }
  };

  const handleUpdateUser = async (id, user) => {
    try {
      await axios.put(`/api/users/${id}`, user);
      fetchUsers();
      setEditingUser(null);
      return true;
    } catch (err) {
      console.error('Error updating user:', err);
      return false;
    }
  };

  const handleDeleteUser = async (id) => {
    if (window.confirm('Are you sure you want to delete this user?')) {
      try {
        await axios.delete(`/api/users/${id}`);
        fetchUsers();
      } catch (err) {
        console.error('Error deleting user:', err);
      }
    }
  };

  const handleEdit = (user) => {
    setEditingUser(user);
  };

  const handleCancelEdit = () => {
    setEditingUser(null);
  };

  return (
    <div className="App">
      <header className="App-header">
        <h1>ICT Project - User Management</h1>
        <p>React + Spring Boot + JDBC + MySQL</p>
      </header>
      
      <div className="container">
        <div className="form-section">
          <UserForm
            onSubmit={editingUser ? (user) => handleUpdateUser(editingUser.id, user) : handleCreateUser}
            editingUser={editingUser}
            onCancel={handleCancelEdit}
          />
        </div>
        
        <div className="list-section">
          {loading && <p>Loading users...</p>}
          {error && <p className="error">{error}</p>}
          {!loading && !error && (
            <UserList
              users={users}
              onEdit={handleEdit}
              onDelete={handleDeleteUser}
            />
          )}
        </div>
      </div>
    </div>
  );
}

export default App;
