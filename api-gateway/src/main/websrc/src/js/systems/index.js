import React from 'react';
import { render } from 'react-dom';
import { Router, Route, Link, browserHistory } from 'react-router';

import SystemList from './routes/system-list';
import SystemDetail from './routes/system-detail';

render((
  <Router history={browserHistory}>
      <Route path="/systems" component={SystemList}/>
      <Route path="/systems/:systemId" component={SystemDetail}/>
  </Router>
), document.getElementById('root'));