const express = require('express');
const { expressjwt: jwt } = require('express-jwt');
const jwksRsa = require('jwks-rsa');
const mysql = require('mysql2/promise');
const envVariables = require('./env-variables.json');
const { default: axios } = require('axios');

const app = express();
app.use(express.json()); // To parse JSON request bodies

// MySQL connection setup
const dbConfig = {
  host: 'localhost',
  port: 3307,
  user: 'root',
  password: 'P@ssw0rd!', // Replace with your MySQL root password
  database: 'jnpj'
};

async function getCurrentUserId() {
  const connection = await mysql.createConnection(dbConfig);
  const [rows] = await connection.execute('SELECT current_user_id FROM user_id_tracker WHERE id = 1');
  await connection.end();
  return rows[0].current_user_id;
}

async function incrementUserId() {
  const connection = await mysql.createConnection(dbConfig);
  await connection.execute('UPDATE user_id_tracker SET current_user_id = current_user_id + 1 WHERE id = 1');
  const [rows] = await connection.execute('SELECT current_user_id FROM user_id_tracker WHERE id = 1');
  await connection.end();
  return rows[0].current_user_id;
}

app.get('/public', (req, res) => res.send('Everyone in the world can read this message.'));

app.use(jwt({
  secret: jwksRsa.expressJwtSecret({
    cache: true,
    rateLimit: true,
    jwksRequestsPerMinute: 60,
    jwksUri: `https://${envVariables.auth0Domain}/.well-known/jwks.json`
  }),
  audience: envVariables.apiIdentifier,
  issuer: `https://${envVariables.auth0Domain}/`,
  algorithms: ['RS256']
}));

// TODO: Remove special console log later
var nodeConsole = require('console');
var myConsole = new nodeConsole.Console(process.stdout, process.stderr);

app.get('/private', (req, res) => res.status(200).send('Only authenticated users can read this message.'));

app.post('/create-user', async (req, res) => {
  try {
    const userId = await getCurrentUserId();
    const newUserId = await incrementUserId(); 

    const nickname = req.body.nickname;
    const refreshToken = req.body.refreshToken;
    const authUserId = req.body.authUserId;

    myConsole.log('Creating user:', userId, nickname, refreshToken);
    myConsole.log(`Truenas link: ${envVariables.truenasApi}/user`);
    myConsole.log(`Truenas API Key: ${envVariables.truenasApiKey}`);
    
    
    const response = await axios.post(`${envVariables.truenasApi}/user`, {
      uid: userId,
      username: `${nickname}_${userId}`,
      group: 45,
      group_create: false,
      /*home: `/mnt/jnpj/${nickname}`,
      home_mode: '0700', // 0700 only user access*/
      shell: '/bin/sh',
      full_name: nickname,
      password: "1234",
      password_disabled: false,
      locked: false,
      microsoft_account: false,
      smb: true,
      sudo: false
    }, {
      headers: {
        'Authorization': `Bearer ${envVariables.truenasApiKey}`,
        'Content-Type': 'application/json'
      }
    });
    
    

    // chech result status, 200 is success
    if (response.status !== 200) {
      myConsole.log('Error creating user:', result);
      res.status(500).send('Error creating user in TrueNAS');
      return;
    }

    const datasetResponse = await axios.post(`${envVariables.truenasApi}/pool/dataset`, {
      name: `jnpj/${nickname}_${userId}`,
      type: 'FILESYSTEM'
    }, {
      headers: {
        'Authorization': `Bearer ${envVariables.truenasApiKey}`,
        'Content-Type': 'application/json'
      }
    });

    if (datasetResponse.status !== 200) {
      myConsole.log('Error creating user:', result);
      res.status(500).send('Error creating user in TrueNAS');
      return;
    }

    //TODO: SET USER PERMS ON DATA SET
    const datasetId = encodeURIComponent(`jnpj/${nickname}_${userId}`);
    const aclResponse = await axios.post(`${envVariables.truenasApi}/pool/dataset/id/${datasetId}/permission`, {
      user: `${nickname}_${userId}`,
      group: 'Auth0', 
      mode: '0700', // Full access for the owner, no access for others
      acl: [],
      options: {
        stripacl: true,
        recursive: true,
        traverse: true
      }
    }, {
      headers: {
        'Authorization': `Bearer ${envVariables.truenasApiKey}`,
        'Content-Type': 'application/json'
      }
    });

    if (aclResponse.status !== 200) {
      myConsole.log('Error setting user perms:', aclResponse.statusText);
      res.status(500).send('Error setting user perms');
      return;
    }

    const managmentApiTokenRequest = await axios.post('https://jnpj-secure-cloud-storage.us.auth0.com/oauth/token', {
        client_id:"w51mJIxOTqy9lM9WJiDvHWWV2AbfIixW",client_secret:"oDWGPPFMRi06X_MIQavKVXfJFEhWchbMbuKuzMtkkA9qY9V0tepDjMbnR_abCfgv",audience:"https://jnpj-secure-cloud-storage.us.auth0.com/api/v2/",grant_type:"client_credentials",
      }, {
        headers: {
          'Content-Type': 'application/json',
      }
    });

    const managmentApiToken = managmentApiTokenRequest.data.access_token;

    myConsole.log('Managment API Token:', managmentApiToken);



    const userResponse = await fetch(`https://jnpj-secure-cloud-storage.us.auth0.com/api/v2/users/${authUserId}`, {
      method: 'PATCH',
      headers: {
        'Authorization': `Bearer ${managmentApiToken}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        user_metadata: {
          uid: `${userId}`,
          drive: `${nickname}_${userId}`
        }
      })
    });
    
    const resultUser = await userResponse.json();


    myConsole.log('User resoponse: ', resultUser);
    res.status(200).json({ uid: userId});
  } catch (error) {
    myConsole.log('Error creating user:', error);
    res.status(500).send(error);
  }
  
});


// TODO: Endpoint to add a directory/share in the user's home directory and to the users meta data
app.post('/create-drive', async (req, res) => {

  const drive = req.body.drive;
  const dataSetName = req.body.dataSetName;
  const authUserId = req.body.authUserId;
  const datasetId = encodeURIComponent(`jnpj/${drive}/${dataSetName}`);
  try {
    const datasetResponse = await fetch(`${envVariables.truenasApi}/pool/dataset`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${envVariables.truenasApiKey}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        name: `jnpj/${drive}/${dataSetName}`,
        type: 'FILESYSTEM'
      })
    });
    
    if (!datasetResponse.ok) {
      myConsole.log('Error creating share:', datasetResponse.statusText);
      res.status(500).send('Error creating share in TrueNAS');
      return;
    }

    
    const aclResponse = await fetch(`${envVariables.truenasApi}/pool/dataset/id/${datasetId}/permission`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${envVariables.truenasApiKey}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        user: `${drive}`,
        group: 'Auth0',
        mode: '0700', // Full access for the owner, no access for others
        acl: [],
        options: {
          stripacl: true,
          recursive: true,
          traverse: true
        }
      })
    });
    
    if (!aclResponse.ok) {
      myConsole.log('Error setting user perms:', aclResponse.statusText);
      res.status(500).send('Error setting user perms');
      return;
    }

    //TODO: ADD DATASET TO USERS METADATA
    const managmentApiTokenRequest = await axios.post('https://jnpj-secure-cloud-storage.us.auth0.com/oauth/token', {
        client_id:"w51mJIxOTqy9lM9WJiDvHWWV2AbfIixW",client_secret:"oDWGPPFMRi06X_MIQavKVXfJFEhWchbMbuKuzMtkkA9qY9V0tepDjMbnR_abCfgv",audience:"https://jnpj-secure-cloud-storage.us.auth0.com/api/v2/",grant_type:"client_credentials",
      }, {
        headers: {
          'Content-Type': 'application/json',
      }
    });

    const managmentApiToken = managmentApiTokenRequest.data.access_token;

    const userMetadataResponse = await fetch(`https://jnpj-secure-cloud-storage.us.auth0.com/api/v2/users/${authUserId}`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${managmentApiToken}`,
        'Content-Type': 'application/json'
      }
    });
    
    const userMetadata = await userMetadataResponse.json();
    const currentDatasets = userMetadata.user_metadata?.datasets || [];

    currentDatasets.push(dataSetName);

    const userResponse = await fetch(`https://jnpj-secure-cloud-storage.us.auth0.com/api/v2/users/${authUserId}`, {
      method: 'PATCH',
      headers: {
        'Authorization': `Bearer ${managmentApiToken}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        user_metadata: {
          datasets: currentDatasets
        }
      })
    });
    res.status(200).send('Share has been created');

  } catch (error) {
    myConsole.log('Error creating share:', error);
    res.status(500).send(error);
  }
});

// ACL DOES NOT NEED TO BE SET!!! STANDARD UNIX PERMS WORK!!!
// NO SHARE NEEDS TO BE CREATED, THE BASE DIRECTORY IS THE SHARE
app.post('/set-user-size', async (req, res) => {
  const uid = req.body.uid;
  const authUserId = req.body.authUserId;
  
  try {
    const response = await axios.post(`${envVariables.truenasApi}/pool/dataset/id/jnpj/set_quota`, [
      {
        quota_type: "USER",
        id: `${uid}`,
        quota_value: 5000000000
      }
    ], {
      headers: {
        'Authorization': `Bearer ${envVariables.truenasApiKey}`,
        'Content-Type': 'application/json'
      }
    });

    if (response.status !== 200) {
      myConsole.log('Error setting user vol size:', response.statusText);
      res.status(500).send(response.statusText);
      return;
    }

    const managmentApiTokenRequest = await axios.post('https://jnpj-secure-cloud-storage.us.auth0.com/oauth/token', {
        client_id:"w51mJIxOTqy9lM9WJiDvHWWV2AbfIixW",client_secret:"oDWGPPFMRi06X_MIQavKVXfJFEhWchbMbuKuzMtkkA9qY9V0tepDjMbnR_abCfgv",audience:"https://jnpj-secure-cloud-storage.us.auth0.com/api/v2/",grant_type:"client_credentials",
      }, {
        headers: {
          'Content-Type': 'application/json',
      }
    });

    const userResponse = await fetch(`https://jnpj-secure-cloud-storage.us.auth0.com/api/v2/users/${authUserId}`, {
      method: 'PATCH',
      headers: {
        'Authorization': `Bearer ${managmentApiTokenRequest.data.access_token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        user_metadata: {
          limited: true
        }
      })
    });

    var resultUser = await userResponse.json();

    if (!resultUser) {
      myConsole.log('Error setting user vol size:', resultUser);
      res.status(500).send(resultUser);
      return;
    }

    res.status(200).send('User vol size has been set');
  } catch (error) {
    myConsole.log('Error setting user vol size:', error);
    res.status(500).send(error);
  }
});

app.put('/set-password', async (req, res) => {
  const uid = req.body.uid;
  const password = "1234";
  myConsole.log('Setting user password:', password);
  try {
    const users = await axios.get(`${envVariables.truenasApi}/user`, {
      headers: {
        'Authorization': `Bearer ${envVariables.truenasApiKey}`,
        'Content-Type': 'application/json'
      }
    });

    var userId = -1;
    users.data.forEach(user => {
      if (user.uid == uid) {
        userId = user.id;
      }
    });

    const response = await axios.put(`${envVariables.truenasApi}/user/id/${userId}`, {
      password: password
    }, {
      headers: {
        'Authorization': `Bearer ${envVariables.truenasApiKey}`,
        'Content-Type': 'application/json'
      }
    });

    if (response.status != 200) {
      myConsole.log('Error setting user password:', response.statusText);
      res.status(500).send(response.statusText);
      return;
    }

    res.status(200).send('User password has been set');
  } catch (error) {
    myConsole.log('Error setting user password:', error);
    res.status(500).send(error);
  }
});


app.listen(3000, () => console.log('Example app listening on port 3000!'));