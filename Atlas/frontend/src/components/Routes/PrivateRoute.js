import React from 'react';
import { Route, Redirect } from 'react-router-dom';

const isAuthenticated = (user) => user !== null && user !== undefined;

const PrivateRoute = ({ component, user, ...rest }) => (
  <Route
    {...rest}
    render={ (props) => isAuthenticated(user)
      ? component
      : <Redirect to={{pathname: '/login', state: {from: props.location}}} />
    }
  />
)
export default PrivateRoute;