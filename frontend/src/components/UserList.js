import React from 'react';
import './UserList.css';

function UserList({ users, onEdit, onDelete }) {
  return (
    <div className="user-list">
      <h2>User List ({users.length})</h2>
      
      {users.length === 0 ? (
        <p className="no-users">No users found. Add a new user to get started!</p>
      ) : (
        <div className="users-grid">
          {users.map(user => (
            <div key={user.id} className="user-card">
              <div className="user-info">
                <h3>{user.name}</h3>
                <p className="user-email">{user.email}</p>
                <p className="user-id">ID: {user.id}</p>
              </div>
              <div className="user-actions">
                <button onClick={() => onEdit(user)} className="btn-edit">
                  Edit
                </button>
                <button onClick={() => onDelete(user.id)} className="btn-delete">
                  Delete
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default UserList;
