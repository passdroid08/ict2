import React, { useState, useEffect } from 'react';
import './UserForm.css';

function UserForm({ onSubmit, editingUser, onCancel }) {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');

  useEffect(() => {
    if (editingUser) {
      setName(editingUser.name);
      setEmail(editingUser.email);
    } else {
      setName('');
      setEmail('');
    }
  }, [editingUser]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!name || !email) {
      alert('Please fill in all fields');
      return;
    }

    const user = { name, email };
    const success = await onSubmit(user);
    
    if (success) {
      setName('');
      setEmail('');
    }
  };

  const handleCancel = () => {
    setName('');
    setEmail('');
    onCancel();
  };

  return (
    <div className="user-form">
      <h2>{editingUser ? 'Edit User' : 'Add New User'}</h2>
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label htmlFor="name">Name:</label>
          <input
            type="text"
            id="name"
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="Enter name"
            required
          />
        </div>
        
        <div className="form-group">
          <label htmlFor="email">Email:</label>
          <input
            type="email"
            id="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="Enter email"
            required
          />
        </div>
        
        <div className="form-buttons">
          <button type="submit" className="btn btn-primary">
            {editingUser ? 'Update' : 'Add'} User
          </button>
          {editingUser && (
            <button type="button" onClick={handleCancel} className="btn btn-secondary">
              Cancel
            </button>
          )}
        </div>
      </form>
    </div>
  );
}

export default UserForm;
